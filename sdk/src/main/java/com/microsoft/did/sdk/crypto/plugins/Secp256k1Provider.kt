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
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.SignatureException
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.stringToByteArray
import com.microsoft.did.sdk.util.toReadableString
import org.bitcoin.NativeSecp256k1
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.spongycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.spongycastle.jcajce.provider.asymmetric.util.EC5Util
import org.spongycastle.jce.ECNamedCurveTable
import org.spongycastle.jce.ECPointUtil
import org.spongycastle.jce.interfaces.ECPublicKey
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECNamedCurveSpec
import org.spongycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
import java.security.spec.X509EncodedKeySpec
import java.util.Locale


class Secp256k1Provider(private val subtleCryptoSha: SubtleCrypto) : Provider() {

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
//        NativeSecp256k1.randomize(seed)

        val secret = ByteArray(32)
        random.nextBytes(secret)

        val ecKeyPair = generateECKeyPair(random)

        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.SHA256.algorithm,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )
        val q = (ecKeyPair.public as BCECPublicKey).q.getEncoded(false)
        val x = q.sliceArray(1..32)
        val y = q.sliceArray(33..64)
            /*(ecKeyPair.public as BCECPublicKey).q.yCoord.encoded*/
        val publicKey = x + y

        val keyPair = CryptoKeyPair(
            privateKey = CryptoKey(
                KeyType.Private,
                extractable,
                signAlgorithm,
                keyUsages.toList(),
                Secp256k1Handle("", (ecKeyPair.private as BCECPrivateKey).d.toByteArray())
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

    private fun generateECKeyPair(random: SecureRandom): KeyPair {
        Security.insertProviderAt(BouncyCastleProvider(), 1)
        val keyGen = KeyPairGenerator.getInstance("EC")
        val ecs = ECGenParameterSpec("secp256k1")
        keyGen.initialize(ecs, random)
        return keyGen.genKeyPair()
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
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }
        val privateKey = generatePrivateKeyFromCryptoKey(key)
        return sign(privateKey, hashedData)
    }

    fun nativeSign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }
        return NativeSecp256k1.sign(hashedData, keyData)
    }

    private fun generatePrivateKeyFromCryptoKey(key: CryptoKey): PrivateKey {
        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: java.security.spec.ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateSpec = java.security.spec.ECPrivateKeySpec(BigInteger((key.handle as Secp256k1Handle).data), curveSpec)
        return keyFactory.generatePrivate(privateSpec)
    }

    private fun generatePublicKeyFromPrivateCryptoKey(privateKey: ByteArray): ByteArray {
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
        val Q = ecSpec.g.multiply(BigInteger(privateKey))
        val pubKeySpec = ECPublicKeySpec(Q, ecSpec)
        val publicKey = keyFactory.generatePublic(pubKeySpec)
        return (publicKey as BCECPublicKey).q.xCoord.encoded +
            publicKey.q.yCoord.encoded
    }

    private fun generatePublicKeyFromCryptoKey(key: CryptoKey): PublicKey {
/*        val curveParams = ECNamedCurveTable.getParameterSpec("secp256k1")
        val curveSpec: java.security.spec.ECParameterSpec =
            ECNamedCurveSpec("secp256k1", curveParams.curve, curveParams.g, curveParams.n, curveParams.h)
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val x = (key.handle as Secp256k1Handle).data.sliceArray(0..31)
        val y = key.handle.data.sliceArray(32..63)
        val publicKeySpec = java.security.spec.ECPublicKeySpec(ECPoint(BigInteger(x), BigInteger(y)), curveSpec)
        return keyFactory.generatePublic(publicKeySpec)*/
        val params = ECNamedCurveTable.getParameterSpec("secp256k1")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val curve = params.curve
        val ellipticCurve: EllipticCurve = EC5Util.convertCurve(curve, params.seed)
        val x = (key.handle as Secp256k1Handle).data.sliceArray(0..31)
        val y = key.handle.data.sliceArray(32..63)
        val encoded = byteArrayOf(0x04)+x+y
        val point: ECPoint = ECPointUtil.decodePoint(ellipticCurve, encoded)
        val params2: ECParameterSpec = EC5Util.convertSpec(ellipticCurve, params)
        val keySpec = java.security.spec.ECPublicKeySpec(point, params2)
        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    private fun sign(privateKey: PrivateKey, payload: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(payload)
        return signature.sign()
    }

    private fun verify(publicKey: PublicKey, payload: ByteArray, sign: ByteArray): Boolean {
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(publicKey)
        signature.update(payload)
        return signature.verify(sign)
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        val publicKey = generatePublicKeyFromCryptoKey(key)
        return verify(publicKey, hashedData, signature)
    }

    override fun nativeVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        SdkLog.d("Key data: " + keyData.toReadableString())
        return NativeSecp256k1.verify(hashedData, signature, keyData)
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
                val x = Base64.decode(keyData.x!!, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val y = Base64.decode(keyData.y!!, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val xyData = ByteArray(64)
                x.forEachIndexed { index, byte ->
                    xyData[index] = byte
                }
                y.forEachIndexed { index, byte ->
                    xyData[index + 32] = byte
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

    override fun checkCryptoKey(key: CryptoKey, keyUsage: KeyUsage) {
        super.checkCryptoKey(key, keyUsage)
        if (key.type == KeyType.Private) {
            val keyData = (key.handle as Secp256k1Handle).data
            if (!NativeSecp256k1.secKeyVerify(keyData)) {
                throw KeyException("Private key invalid")
            }
        }
    }

    // mapped from secp256k1_eckey_pubkey_parse
    private fun publicToXY(keyData: ByteArray): Pair<String, String> {
        // uncompressed, bytes 1-32, and 33-end are x and y
        val x = keyData.sliceArray(0..31)
        val y = keyData.sliceArray(32..63)
        return Pair(
            Base64.encodeToString(x, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
            Base64.encodeToString(y, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        )
    }

    enum class secp256k1Tag(val byte: Byte) {
        even(0x02),
        odd(0x03),
        uncompressed(0x04),
        hybridEven(0x06),
        hybridOdd(0x07)
    }
}