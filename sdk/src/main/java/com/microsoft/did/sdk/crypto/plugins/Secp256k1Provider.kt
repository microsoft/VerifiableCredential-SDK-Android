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
import com.nimbusds.jose.util.Base64URL
import org.bitcoin.NativeSecp256k1
import org.bouncycastle.util.encoders.Hex
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
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.ECPointUtil
import org.spongycastle.jce.interfaces.ECPublicKey
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECNamedCurveSpec
import org.spongycastle.jce.spec.ECPublicKeySpec
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
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
        val seed = ByteArray(32)
        val random = SecureRandom()
        random.nextBytes(seed)

        val secret = ByteArray(32)
        random.nextBytes(secret)

        val ecKeyPair = generateECKeyPair(random)

        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        val privateKey = (ecKeyPair.private as ECPrivateKeyParameters).d.toByteArray()
        val publicKey = (ecKeyPair.public as ECPublicKeyParameters).q.getEncoded(false)

        val keyPair = CryptoKeyPair(
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
        return keyPair
    }

        fun parseBigIntegerPositive(b: BigInteger): ByteArray {
            var inner = b
        if (inner.compareTo(BigInteger.ZERO) < 0)
            inner = inner.add(TWO_COMPL_REF);

       val unsignedbyteArray= b.toByteArray();
        return unsignedbyteArray;
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
/*        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(data)
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(generatePrivateKeyFromByteArray((key.handle as Secp256k1Handle).data))
        signature.update(hashed)
        val rs = decodeFromDER(signature.sign())
        return rs.first.toByteArray()+rs.second.toByteArray()*/
        val signingSigner = ECDSASigner()
        val ecParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val ecDomainParameters = ECDomainParameters(ecParams.curve, ecParams.g, ecParams.n, ecParams.h)
        val privateKeyParams = ECPrivateKeyParameters(BigInteger((key.handle as Secp256k1Handle).data), ecDomainParameters)
        signingSigner.init(true, privateKeyParams)
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(data)
        val components = signingSigner.generateSignature(hashed)
        val r = components[0]
        var s = components[1]
        if(components[1] > ecParams.n.shiftRight(1))
            println("S is negative")
//            s = ecDomainParameters.n.subtract(components[1])
        var rArr = r.toByteArray()
        var sArr = s.toByteArray()
        if(rArr.size > 32)
            rArr = rArr.sliceArray(1 until rArr.size)
        if(sArr.size > 32)
            sArr = sArr.sliceArray(1 until sArr.size)
        return rArr+sArr
//        return encodeToDer(components[0].toByteArray(), components[1].toByteArray())
    }

    private fun generatePrivateKeyFromCryptoKey(key: CryptoKey): PrivateKey {
        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: java.security.spec.ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateSpec = java.security.spec.ECPrivateKeySpec(BigInteger((key.handle as Secp256k1Handle).data), curveSpec)
        return keyFactory.generatePrivate(privateSpec)
    }

    private fun generatePrivateKeyFromByteArray(key: ByteArray): PrivateKey {
        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateSpec = java.security.spec.ECPrivateKeySpec(BigInteger(key), curveSpec)
        return keyFactory.generatePrivate(privateSpec)
    }

    private fun generatePublicKeyFromPrivateCryptoKey(privateKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
        val Q = ecSpec.g.multiply(BigInteger(privateKey))
        val pubKeySpec = ECPublicKeySpec(Q, ecSpec)
        val publicKey = keyFactory.generatePublic(pubKeySpec)
        return byteArrayOf(0x04)+(publicKey as BCECPublicKey).q.xCoord.encoded +
            publicKey.q.yCoord.encoded
    }

    private fun generatePublicKeyFromCryptoKey(key: CryptoKey): PublicKey {
        val params = ECNamedCurveTable.getParameterSpec("secp256k1")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val curve = params.curve
        val ellipticCurve: EllipticCurve = EC5Util.convertCurve(curve, params.seed)
        val x = (key.handle as Secp256k1Handle).data.sliceArray(0..31)
        val y = key.handle.data.sliceArray(32..63)
        val encoded = byteArrayOf(0x04) + x + y
        val point: ECPoint = ECPointUtil.decodePoint(ellipticCurve, encoded)
        val params2: ECParameterSpec = EC5Util.convertSpec(ellipticCurve, params)
        val keySpec = java.security.spec.ECPublicKeySpec(point, params2)
        return keyFactory.generatePublic(keySpec) as ECPublicKey
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
//        val rs = decodeFromDER(signature)
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(data)
        return verifySigner.verifySignature(hashed, BigInteger(signature.sliceArray(0 until signature.size/2)), BigInteger(signature.sliceArray(
            signature.size/2 until signature.size
        )))
    }

    override fun nativeVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val digest = MessageDigest.getInstance(W3cCryptoApiConstants.Sha256.value)
        val hashed = digest.digest(data)
        return NativeSecp256k1.verify(hashed, signature, (key.handle as Secp256k1Handle).data)
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
                xyData[0] = secp256k1Tag.uncompressed.byte
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
                keyData[0] == secp256k1Tag.uncompressed.byte ||
                    keyData[0] == secp256k1Tag.hybridOdd.byte ||
                    keyData[0] == secp256k1Tag.hybridEven.byte
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

    enum class secp256k1Tag(val byte: Byte) {
        even(0x02),
        odd(0x03),
        uncompressed(0x04),
        hybridEven(0x06),
        hybridOdd(0x07)
    }
}