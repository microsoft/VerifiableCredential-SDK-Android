package com.microsoft.portableIdentity.sdk.crypto.plugins

import android.annotation.TargetApi
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.microsoft.portableIdentity.sdk.crypto.models.AndroidConstants
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.AndroidKeyHandle
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithms.AesKeyGenParams
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.portableIdentity.sdk.utilities.AndroidKeyConverter
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import java.math.BigInteger
import java.security.*
import java.security.spec.*
import javax.crypto.KeyGenerator

class AndroidSubtle(private var keyStore: AndroidKeyStore, private val logger: ILogger): SubtleCrypto {
    override fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): ByteArray {
        // verify we're signing with a private key
        if (key.type != KeyType.Private) {
            throw logger.error("Sign must use a private key")
        }
        // key's handle should be an Android keyStore key reference.
        val handle = cryptoKeyToPrivateKey(key)
        return Signature.getInstance(signAlgorithmToAndroid(algorithm, key)).run {
            initSign(handle)
            update(data)
            sign()
        }
    }

    override fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Boolean {
        val handle = cryptoKeyToPublicKey(key)
        val s = Signature.getInstance(signAlgorithmToAndroid(algorithm, key)).apply {
            initVerify(handle)
            update(data)
        }
        return s.verify(signature)
    }

    override fun digest(algorithm: Algorithm, data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(algorithm.name)
        return digest.digest(data)
    }

    override fun generateKey(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKey {
        val secret = when(algorithm.name) {
            W3cCryptoApiConstants.AesCbc.value, W3cCryptoApiConstants.AesCtr.value,
            W3cCryptoApiConstants.AesGcm.value, W3cCryptoApiConstants.AesKw.value -> {
                val generator = KeyGenerator.getInstance(AndroidConstants.Aes.value)
                val alg = algorithm as AesKeyGenParams
                generator.init(alg.length.toInt())
                generator.generateKey()
            }
            else -> throw logger.error("Unsupported symmetric key algorithm: ${algorithm.name}")
        }
        return CryptoKey(
            type = KeyType.Secret,
            extractable = extractable,
            usages = keyUsages,
            handle = secret,
            algorithm = algorithm
        )
    }

    @TargetApi(23)
    override fun generateKeyPair(algorithm: Algorithm, extractable: Boolean, keyUsages: List<KeyUsage>): CryptoKeyPair {
        if (!algorithm.additionalParams.containsKey(AndroidConstants.KeyReference.value)){
            throw logger.error("Algorithm must contain an additional parameter \"${AndroidConstants.KeyReference.value}\"")
        }
        val alias = keyStore.checkOrCreateKeyId(algorithm.additionalParams[AndroidConstants.KeyReference.value] as String, null)
        logger.debug("Generating ${algorithm.name} key with alias $alias")
        val keyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgorithmToAndroid(algorithm), AndroidKeyStore.provider)
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                keyPairUsageToAndroid(keyUsages)
            ).setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .build()
        )
        val keyPair = keyPairGenerator.genKeyPair()
//        AndroidKeyStore.keyStore.
        logger.debug("Key pair generated ($alias)")
        // convert keypair.
        return CryptoKeyPair(
            CryptoKey(
                KeyType.Public,
                extractable,
                algorithm,
                keyUsages,
                AndroidKeyHandle(
                    alias,
                    keyPair.public
                )
            ),
            CryptoKey(
                KeyType.Private,
                false,
                algorithm,
                keyUsages,
                AndroidKeyHandle(
                    alias,
                    null
                )
            )
        )
    }

    override fun deriveKey(
        algorithm: Algorithm,
        baseKey: CryptoKey,
        derivedKeyType: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deriveBits(algorithm: Algorithm, baseKey: CryptoKey, length: ULong): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importKey(
        format: KeyFormat,
        keyData: ByteArray,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun importKey(
        format: KeyFormat,
        keyData: JsonWebKey,
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        when (keyData.kty) {
            com.microsoft.portableIdentity.sdk.crypto.keys.KeyType.RSA.value -> {
                val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
                if (keyData.d != null) { // Private RSA key being imported
                    if (!AndroidKeyStore.keyStore.isKeyEntry(keyData.kid ?: "")) {
                        throw logger.error("Software private keys are not supported.")
                    }
                    val entry = AndroidKeyHandle(keyData.kid!!, null)
                    return CryptoKey(
                        KeyType.Private,
                        extractable,
                        JwaCryptoConverter.jwaAlgToWebCrypto(keyData.alg!!, logger = logger),
                        keyUsages,
                        entry
                    )
                } else { // Public RSA key being imported
                    val key = keyFactory.generatePublic(RSAPublicKeySpec(
                        BigInteger(1, Base64.decode(keyData.n, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)),
                        BigInteger(1, Base64.decode(keyData.e, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
                    ))
                    val entry = AndroidKeyHandle(keyData.kid ?: "", key)
                    return CryptoKey(
                        KeyType.Public,
                        extractable,
                        JwaCryptoConverter.jwaAlgToWebCrypto(keyData.alg!!, logger = logger),
                        keyUsages,
                        entry
                    )
                }
            }
            com.microsoft.portableIdentity.sdk.crypto.keys.KeyType.EllipticCurve.value -> {
                TODO("Standard Elliptic Curves are not currently supported.")
            }
            else -> throw logger.error("Cannot import JWK key type ${keyData.kty}")
        }
    }

    override fun exportKey(format: KeyFormat, key: CryptoKey): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exportKeyJwk(key: CryptoKey): JsonWebKey {
        val internalHandle = key.handle as? AndroidKeyHandle ?: throw logger.error("Unknown format for CryptoKey passed")
        return when (internalHandle.key) {
            is PublicKey -> {
                AndroidKeyConverter.androidPublicKeyToPublicKey(internalHandle.alias, internalHandle.key, logger).toJWK()
            }
            null -> {
                AndroidKeyConverter.androidPrivateKeyToPrivateKey(internalHandle.alias, AndroidKeyStore.keyStore, logger).toJWK()
            }
            else -> {
                throw logger.error("Unknown CryptoKey format")
            }
        }
    }

    override fun wrapKey(
        format: KeyFormat,
        key: CryptoKey,
        wrappingKey: CryptoKey,
        wrapAlgorithm: Algorithm
    ): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unwrapKey(
        format: KeyFormat,
        wrappedKey: ByteArray,
        unwrappingKey: CryptoKey,
        unwrapAlgorithm: Algorithm,
        unwrappedKeyAlgorithm: Algorithm,
        extractable: Boolean,
        keyUsages: List<KeyUsage>
    ): CryptoKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun cryptoKeyToPublicKey(key: CryptoKey): PublicKey {
        val internalHandle = key.handle as? AndroidKeyHandle
            ?: throw logger.error("Unknown format for CryptoKey passed")
        return internalHandle.key as? PublicKey
            ?: throw logger.error("Private key passed when a public key was expected")
    }

    private fun cryptoKeyToPrivateKey(key: CryptoKey): PrivateKey {
        val internalHandle = key.handle as? AndroidKeyHandle
            ?: throw logger.error("Unknown format for CryptoKey passed")
        return AndroidKeyStore.keyStore.getKey(internalHandle.alias, null) as? PrivateKey
            ?: throw logger.error("Software private keys are not supported by the native Subtle.")
    }


    private fun signAlgorithmToAndroid(algorithm: Algorithm, cryptoKey: CryptoKey): String {
        return when (algorithm.name) {
            W3cCryptoApiConstants.EcDsa.value -> {
                val ecDsaParams = algorithm as EcdsaParams
                when (ecDsaParams.hash.name) {
                    W3cCryptoApiConstants.Sha1.value -> AndroidConstants.EcDsaSha1.value
                    W3cCryptoApiConstants.Sha224.value -> AndroidConstants.EcDsaSha224.value
                    W3cCryptoApiConstants.Sha256.value -> AndroidConstants.EcDsaSha256.value
                    W3cCryptoApiConstants.Sha384.value -> AndroidConstants.EcDsaSha384.value
                    W3cCryptoApiConstants.Sha512.value -> AndroidConstants.EcDsaSha512.value
                    else -> throw logger.error("Unsupported ECDSA hash algorithm: ${ecDsaParams.hash.name}")
                }
            }
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value -> {
                // The hash is indicated by the key's "algorithm" slot.
                val keyAlgorithm = cryptoKey.algorithm as? RsaHashedKeyAlgorithm ?: throw logger.error("Unsupported RSA key algorithm: ${cryptoKey.algorithm.name}")
                when (keyAlgorithm.hash.name) {
                    W3cCryptoApiConstants.Sha1.value -> AndroidConstants.RsSha1.value
                    W3cCryptoApiConstants.Sha224.value -> AndroidConstants.RsSha224.value
                    W3cCryptoApiConstants.Sha256.value -> AndroidConstants.RsSha256.value
                    W3cCryptoApiConstants.Sha384.value -> AndroidConstants.RsSha384.value
                    W3cCryptoApiConstants.Sha512.value -> AndroidConstants.RsSha512.value
                    else -> throw logger.error("Unsupported RSA hash algorithm: ${keyAlgorithm.hash.name}")
                }
            }
            else -> throw logger.error("Unsupported algorithm: ${algorithm.name}")
        }
    }

    private fun keyPairAlgorithmToAndroid(algorithm: Algorithm): String {
        return when (algorithm.name) {
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value -> AndroidConstants.Rsa.value
            W3cCryptoApiConstants.EcDsa.value -> AndroidConstants.Ec.value
            else -> throw logger.error("Unknown algorithm used: ${algorithm.name}")
        }
    }

    private fun keyPairUsageToAndroid(usages: List<KeyUsage>): Int {
        var flags = 0
        usages.forEach { usage ->
            flags = flags.or(when (usage) {
                KeyUsage.Decrypt -> KeyProperties.PURPOSE_DECRYPT
                KeyUsage.Encrypt -> KeyProperties.PURPOSE_ENCRYPT
                KeyUsage.Sign -> KeyProperties.PURPOSE_SIGN
                KeyUsage.Verify -> KeyProperties.PURPOSE_VERIFY
                KeyUsage.WrapKey -> KeyProperties.PURPOSE_ENCRYPT
                KeyUsage.UnwrapKey -> KeyProperties.PURPOSE_DECRYPT
                else -> 0
            })
        }
        return flags
    }
}