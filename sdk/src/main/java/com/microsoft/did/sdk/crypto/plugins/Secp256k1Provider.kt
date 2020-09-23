// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import android.util.Base64
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcdsaParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.util.Constants.SECP256K1_CURVE_NAME_EC
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import com.microsoft.did.sdk.util.controlflow.SignatureException
import com.microsoft.did.sdk.util.convertSignedToUnsignedByteArray
import com.microsoft.did.sdk.util.generatePublicKeyFromPrivateKey
import com.microsoft.did.sdk.util.publicToXY
import com.microsoft.did.sdk.util.stringToByteArray
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.asn1.DLSequence
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.SecureRandom
import java.security.Security
import java.util.Locale

class Secp256k1Provider(private val subtleCryptoSha: SubtleCrypto) : Provider() {
    init {
        Security.insertProviderAt(BouncyCastleProvider(), Security.getProviders().size + 1)
    }

    data class Secp256k1Handle(val alias: String, val data: ByteArray)

    override val name: String = "ECDSA"
    override val privateKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Sign)
    override val publicKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Verify)
    override val symmetricKeyUsage: Set<KeyUsage>? = null

    override fun onGenerateKeyPair(
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKeyPair {
        val random = SecureRandom()
        val secret = ByteArray(32)
        random.nextBytes(secret)

        val publicKey = generatePublicKeyFromPrivateKey(secret)

        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        return CryptoKeyPair(
            privateKey = CryptoKey(
                KeyType.Private,
                extractable,
                signAlgorithm,
                keyUsages.toList(),
                Secp256k1Handle("", secret)
            ),
            publicKey = CryptoKey(
                KeyType.Public,
                true,
                signAlgorithm,
                publicKeyUsage.toList(),
                Secp256k1Handle("", publicKey)
            )
        )
    }

    override fun checkGenerateKeyParams(algorithm: Algorithm) {
        val keyGenParams = algorithm as? EcKeyGenParams ?: throw AlgorithmException("EcKeyGenParams expected as algorithm")
        if (keyGenParams.namedCurve.toUpperCase(Locale.ROOT) != W3cCryptoApiConstants.Secp256k1.value.toUpperCase(Locale.ROOT) &&
            keyGenParams.namedCurve.toUpperCase(Locale.ROOT) != W3cCryptoApiConstants.Secp256k1.name.toUpperCase(Locale.ROOT)
        ) {
            throw AlgorithmException("The curve ${keyGenParams.namedCurve} is not supported by Secp256k1Provider")
        }
    }

    override fun onSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        val signingSigner = ECDSASigner()
        val ecDomainParameters = createCurveParameters()
        val privateKeyParams = ECPrivateKeyParameters(BigInteger(1, keyData), ecDomainParameters)
        signingSigner.init(true, privateKeyParams)

        val signature = signingSigner.generateSignature(hashedData)
        return convertSignatureToUnsignedByteArray(signature)
    }

    /**
     * Signature is returned as array of BigIntegers for R and S. Converting them into unsigned byte array.
     */
    private fun convertSignatureToUnsignedByteArray(signature: Array<BigInteger>): ByteArray {
        var r = convertSignedToUnsignedByteArray(signature[0].toByteArray())
        var s = convertSignedToUnsignedByteArray(signature[1].toByteArray())
        return r + s
    }

    private fun createCurveParameters(): ECDomainParameters {
        val ecParams = ECNamedCurveTable.getParameterSpec(SECP256K1_CURVE_NAME_EC)
        return ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
    }

    //TODO: Incorporate der encoding while adding support for interoperability
    fun encodeToDer(r: ByteArray, s: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(72)
        val seq = DERSequenceGenerator(bos)
        seq.addObject(ASN1Integer(r))
        seq.addObject(ASN1Integer(s))
        seq.close()
        return bos.toByteArray()
    }

    fun decodeFromDER(bytes: ByteArray): Pair<BigInteger, BigInteger> {
        val decoder = ASN1InputStream(bytes)
        val seq = decoder.readObject() as DLSequence
        val r = seq.getObjectAt(0) as ASN1Integer
        val s = seq.getObjectAt(1) as ASN1Integer
        return Pair(r.positiveValue, s.positiveValue)
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        if (signature.size > 64) {
            throw SignatureException("Signature is not in R|S format")
        }
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        val verifySigner = ECDSASigner()
        val ecDomainParameters = createCurveParameters()
        val publicKeyParams = ECPublicKeyParameters(ecDomainParameters.curve.decodePoint(keyData), ecDomainParameters)
        verifySigner.init(false, publicKeyParams)

        return verifySigner.verifySignature(
            hashedData,
            BigInteger(1, signature.sliceArray(0 until signature.size / 2)),
            BigInteger(1, signature.sliceArray(signature.size / 2 until signature.size))
        )
    }

    override fun onImportKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Set<KeyUsage>
    ): CryptoKey {
        val alias = keyData.kid ?: ""
        return when {
            keyData.d != null -> { // import d as the private key handle
                CryptoKey(
                    type = KeyType.Private,
                    extractable = extractable,
                    algorithm = algorithm,
                    usages = keyUsages.toList(),
                    handle = Secp256k1Handle(
                        alias,
                        Base64.decode(stringToByteArray(keyData.d!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                    )
                )
            }
            keyData.k != null -> { // import k as the secret key handle
                CryptoKey(
                    type = KeyType.Secret,
                    extractable = extractable,
                    algorithm = algorithm,
                    usages = keyUsages.toList(),
                    handle = Secp256k1Handle(
                        alias,
                        Base64.decode(stringToByteArray(keyData.k!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                    )
                )
            }
            else -> {// public key
                val x = Base64.decode(stringToByteArray(keyData.x!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val y = Base64.decode(stringToByteArray(keyData.y!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val xyData = ByteArray(65)
                xyData[0] = Secp256k1Tag.UNCOMPRESSED.byte
                x.forEachIndexed { index, byte ->
                    xyData[index + 1] = byte
                }
                y.forEachIndexed { index, byte ->
                    xyData[index + 33] = byte
                }
                CryptoKey(
                    type = KeyType.Public,
                    extractable = extractable,
                    algorithm = algorithm,
                    usages = keyUsages.toList(),
                    handle = Secp256k1Handle(alias, xyData)
                )
            }
        }
    }

    override fun onExportKeyJwk(key: CryptoKey): JsonWebKey {
        val keyOps = mutableListOf<String>()
        for (usage in key.usages) {
            keyOps.add(usage.value)
        }
        val publicKey: ByteArray
        val handle = key.handle as Secp256k1Handle
        val d: String? = if (key.type == KeyType.Private) {
            publicKey = generatePublicKeyFromPrivateKey(handle.data)
            Base64.encodeToString(handle.data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        } else {
            publicKey = handle.data
            null
        }
        val xyData = publicToXY(publicKey)
        return JsonWebKey(
            kty = com.microsoft.did.sdk.crypto.keys.KeyType.EllipticCurve.value,
            kid = handle.alias,
            crv = W3cCryptoApiConstants.Secp256k1.value,
            use = "sig",
            key_ops = keyOps,
            alg = JwaCryptoConverter.webCryptoToJwa(key.algorithm),
            ext = key.extractable,
            d = d?.trim(),
            x = xyData.first.trim(),
            y = xyData.second.trim()
        )
    }

    enum class Secp256k1Tag(val byte: Byte) {
        EVEN(0x02),
        ODD(0x03),
        UNCOMPRESSED(0x04),
        HYBRID_EVEN(0x06),
        HYBRID_ODD(0x07)
    }
}