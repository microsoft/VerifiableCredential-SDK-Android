// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.crypto.plugins

//import org.web3j.crypto.*
import android.util.Base64
import com.microsoft.portableIdentity.sdk.crypto.models.AndroidConstants
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.CryptoKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.EcKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.EcdsaParams
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyType
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.portableIdentity.sdk.utilities.controlflow.AlgorithmException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.KeyException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.KeyFormatException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.SignatureException
import com.microsoft.portableIdentity.sdk.utilities.printBytes
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray
import org.bitcoin.NativeSecp256k1
import org.spongycastle.jce.provider.BouncyCastleProvider
import org.spongycastle.jce.spec.ECNamedCurveGenParameterSpec
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPublicKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Locale


class Secp256k1Provider(val subtleCryptoSha: SubtleCrypto) : Provider() {

    data class Secp256k1Handle(val alias: String, val data: ByteArray)

    override val name: String = "ECDSA"
    override val privateKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Sign)
    override val publicKeyUsage: Set<KeyUsage> = setOf(KeyUsage.Verify)
    override val symmetricKeyUsage: Set<KeyUsage>? = null
    init {
            Security.addProvider(BouncyCastleProvider())
    }


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

//        val publicKey = NativeSecp256k1.computePubkey(secret)
/*        val privateKey = BigInteger(1, secret.copyOfRange(0, secret.size))
        val publicKey = Sign.publicKeyFromPrivate(privateKey)*/

        val ecKeyPair = generateECKeyPair(random)

        val signAlgorithm = EcdsaParams(
            hash = algorithm.additionalParams["hash"] as? Algorithm ?: Sha.Sha256,
            additionalParams = mapOf(
                "namedCurve" to W3cCryptoApiConstants.Secp256k1.value
            )
        )

        val keyPair = CryptoKeyPair(
            privateKey = CryptoKey(
                KeyType.Private,
                extractable,
                signAlgorithm,
                keyUsages.toList(),
                Secp256k1Handle("", ecKeyPair.private.encoded)
            ),
            publicKey = CryptoKey(
                KeyType.Public,
                true,
                signAlgorithm,
                publicKeyUsage.toList(),
                Secp256k1Handle("", ecKeyPair.public.encoded)
            )
        )

        return keyPair
    }

    private fun generateECKeyPair(random: SecureRandom): KeyPair {
//        val keyPair = KeyPairGenerator.getInstance(AndroidConstants.Ec.value, "BC")
/*        val keyPair = KeyPairGenerator.getInstance(AndroidConstants.Ec.value)
        //TODO: Verify if algorithm name is Secp256k1 or P-256K
        val ecs = ECGenParameterSpec("secp256k1")
        keyPair.initialize(ecs, SecureRandom())
        return keyPair.generateKeyPair()*/
//        Security.addProvider(BouncyCastleProvider())
        val keyGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider())
        val ecs = ECNamedCurveGenParameterSpec("secp256k1")
        keyGen.initialize(ecs, SecureRandom())
        val pair = keyGen.genKeyPair()
        return pair
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
//        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }
//        return NativeSecp256k1.sign(hashedData, keyData)
        val privateKey = generatePrivateKeyFromCryptoKey(key)
        return sign(privateKey, hashedData)
    }

    private fun generatePrivateKeyFromCryptoKey(key: CryptoKey): PrivateKey {
//        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value, "BC")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val privateKeySpec = PKCS8EncodedKeySpec((key.handle as Secp256k1Handle).data)
        return keyFactory.generatePrivate(privateKeySpec)
    }

    private fun generatePublicKeyFromPrivateCryptoKey(privateKey: ByteArray): PublicKey {
//        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value, "BC")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
/*        val privateKeySpec = PKCS8EncodedKeySpec(privateKey)
        return keyFactory.generatePublic(privateKeySpec)*/
        val privateKeySpec = X509EncodedKeySpec(privateKey)
        return keyFactory.generatePublic(privateKeySpec)
    }

    private fun generatePublicKeyFromCryptoKey(key: CryptoKey): PublicKey {
//        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value, "BC")
        val keyFactory = KeyFactory.getInstance(AndroidConstants.Ec.value)
        val publicKeySpec = X509EncodedKeySpec((key.handle as Secp256k1Handle).data)
        return keyFactory.generatePublic(publicKeySpec)
    }

    private fun sign(privateKey: PrivateKey, payload: ByteArray): ByteArray {
//        val signature = Signature.getInstance("SHA256withECDSA", "BC")
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        signature.update(payload)
        return signature.sign()
    }

    private fun verify(publicKey: PublicKey, payload: ByteArray, sign: ByteArray): Boolean {
//        val signature = Signature.getInstance("SHA256withECDSA", "BC")
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initVerify(publicKey)
        signature.update(payload)
        return signature.verify(sign)
    }

    override fun onVerify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val keyData = (key.handle as Secp256k1Handle).data
        val ecAlgorithm = algorithm as EcdsaParams
        val hashedData = subtleCryptoSha.digest(ecAlgorithm.hash, data)
        if (hashedData.size != 32) {
            throw SignatureException("Data must be 32 bytes")
        }

        print("KEY DATA: ")
        printBytes(keyData)

//        return NativeSecp256k1.verify(hashedData, signature, keyData)
        val publicKey = generatePublicKeyFromCryptoKey(key)
        return verify(publicKey, hashedData, signature)
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
                val x =
                    Base64.decode(stringToByteArray(keyData.x!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val y =
                    Base64.decode(stringToByteArray(keyData.y!!), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
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
            //publicKey = NativeSecp256k1.computePubkey(handle.data)
            publicKey = generatePublicKeyFromPrivateCryptoKey(handle.data).encoded
            Base64.encodeToString(handle.data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        } else {
            publicKey = handle.data
            null
        }
        val xyData = publicToXY(publicKey)
        return JsonWebKey(
            kty = com.microsoft.portableIdentity.sdk.crypto.keys.KeyType.EllipticCurve.value,
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
        if (keyData.size == 33 && (
                keyData[0] == secp256k1Tag.even.byte ||
                    keyData[0] == secp256k1Tag.odd.byte)
        ) {
            // compressed form
            throw KeyFormatException("Compressed Hex format is not supported.")
        } else if (keyData.size == 65 && (
                keyData[0] == secp256k1Tag.uncompressed.byte ||
                    keyData[0] == secp256k1Tag.hybridEven.byte ||
                    keyData[0] == secp256k1Tag.hybridOdd.byte
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