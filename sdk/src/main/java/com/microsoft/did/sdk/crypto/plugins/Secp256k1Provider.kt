// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.plugins

import android.util.Base64
import com.microsoft.did.sdk.crypto.models.AndroidConstants
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
import com.microsoft.did.sdk.util.controlflow.AlgorithmException
import com.microsoft.did.sdk.util.controlflow.KeyFormatException
import com.microsoft.did.sdk.util.controlflow.SignatureException
import com.microsoft.did.sdk.util.stringToByteArray
import org.spongycastle.asn1.ASN1InputStream
import org.spongycastle.asn1.ASN1Integer
import org.spongycastle.asn1.DERSequenceGenerator
import org.spongycastle.asn1.DLSequence
import org.spongycastle.crypto.AsymmetricCipherKeyPair
import org.spongycastle.crypto.generators.ECKeyPairGenerator
import org.spongycastle.crypto.params.ECDomainParameters
import org.spongycastle.crypto.params.ECKeyGenerationParameters
import org.spongycastle.crypto.params.ECPrivateKeyParameters
import org.spongycastle.crypto.params.ECPublicKeyParameters
import org.spongycastle.crypto.signers.ECDSASigner
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECPublicKeySpec
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Security
import java.util.Locale


class Secp256k1Provider(private val subtleCryptoSha: SubtleCrypto) : Provider() {

    private val TWO_COMPL_REF = BigInteger.ONE.shiftLeft(64)

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
        val seed = random.generateSeed(32)
        random.nextBytes(seed)

        val secret = ByteArray(32)
        random.nextBytes(secret)

//        val ecKeyPair = generateECKeyPair(random)

        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        val privateKey = secret
//        val privateKey = parseBigIntegerPositive((ecKeyPair.private as ECPrivateKeyParameters).d)
/*        val x = (ecKeyPair.public as ECPublicKeyParameters).q.normalize().xCoord.encoded
        val y = (ecKeyPair.public as ECPublicKeyParameters).q.normalize().yCoord.encoded*/
//        val publicKey = byteArrayOf(Secp256k1Tag.UNCOMPRESSED.byte)+x+y
        val publicKey = generatePublicKeyFromPrivateCryptoKey(privateKey)

        return CryptoKeyPair(
            privateKey = CryptoKey(
                KeyType.Private,
                extractable,
                signAlgorithm,
                keyUsages.toList(),
                Secp256k1Handle("", privateKey)
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

    private fun parseBigIntegerPositive(b: BigInteger): ByteArray {
        var inner = b
        if (inner < BigInteger.ZERO)
            inner = inner.add(TWO_COMPL_REF)

        return b.toByteArray()
    }

    private fun generateECKeyPair(random: SecureRandom): AsymmetricCipherKeyPair {
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        val keyGen = ECKeyPairGenerator()
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val gParam = ECKeyGenerationParameters(ecDomainParameters, random)
        keyGen.init(gParam)
        return keyGen.generateKeyPair()
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
        val signingSigner = ECDSASigner()
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val privateKeyParams = ECPrivateKeyParameters(BigInteger(1, (key.handle as Secp256k1Handle).data), ecDomainParameters)
        signingSigner.init(true, privateKeyParams)
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(data)
        val components = signingSigner.generateSignature(hashed)
        var r = components[0].toByteArray()
        var s = components[1].toByteArray()
        if(r.size > 32)
            r = r.sliceArray(1 until r.size)
        if(s.size > 32)
            s = s.sliceArray(1 until s.size)
        return r+s
    }

    private fun generatePublicKeyFromPrivateCryptoKey(privateKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecPoint = ecSpec.g.multiply(BigInteger(1, privateKey))
        val publicDerBytes = ecPoint.getEncoded(false)
        val point = ecSpec.curve.decodePoint(publicDerBytes)
        val pubKeySpec = ECPublicKeySpec(point, ecSpec)
        val publicKey = keyFactory.generatePublic(pubKeySpec)
        return byteArrayOf(Secp256k1Tag.UNCOMPRESSED.byte)+(publicKey as BCECPublicKey).q.xCoord.encoded +
            publicKey.q.yCoord.encoded
    }

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
        try {
            val seq = decoder.readObject() as DLSequence
            val r = seq.getObjectAt(0) as ASN1Integer
            val s = seq.getObjectAt(1) as ASN1Integer
            return Pair(r.positiveValue, s.positiveValue)
        } catch (e: IOException) {
            throw RuntimeException(e);
        } finally {
            if (decoder != null)
                try {
                    decoder.close()
                } catch (x: IOException) {
                }
        }
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        if(signature.size > 64) {
            throw SignatureException("Signature is not in R|S format")
        }
        val verifySigner = ECDSASigner()
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curve = ecParams.curve
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val params = ECPublicKeyParameters(curve.decodePoint((key.handle as Secp256k1Handle).data), ecDomainParameters)
        verifySigner.init(false, params)
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(data)
        return verifySigner.verifySignature(hashed, BigInteger(1, signature.sliceArray(0 until signature.size/2)), BigInteger(1, signature.sliceArray(
            signature.size/2 until signature.size
        )))
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
                        Base64.decode(
                            stringToByteArray(keyData.d!!),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        )
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
                        Base64.decode(
                            stringToByteArray(keyData.k!!),
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        )
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
            publicKey = generatePublicKeyFromPrivateCryptoKey(handle.data)
            Base64.encodeToString(
                handle.data,
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
            )
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

    // mapped from secp256k1_eckey_pubkey_parse
    private fun publicToXY(keyData: ByteArray): Pair<String, String> {
        if (keyData.size == 65 && (
                keyData[0] == Secp256k1Tag.UNCOMPRESSED.byte ||
                    keyData[0] == Secp256k1Tag.HYBRID_ODD.byte ||
                    keyData[0] == Secp256k1Tag.HYBRID_EVEN.byte
                )
        ) {
            // uncompressed, bytes 1-32, and 33-end are x and y
            val x = keyData.sliceArray(1..32)
            val y = keyData.sliceArray(33..64)
            return Pair(
                Base64.encodeToString(x, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
                Base64.encodeToString(y, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            )
        } else {
            throw KeyFormatException("Public key improperly formatted")
        }
    }

    enum class Secp256k1Tag(val byte: Byte) {
        EVEN(0x02),
        ODD(0x03),
        UNCOMPRESSED(0x04),
        HYBRID_EVEN(0x06),
        HYBRID_ODD(0x07)
    }
}