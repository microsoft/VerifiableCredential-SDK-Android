package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoFactory
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.ILogger

/**
 * Class that encompasses all of Crypto
 * @param subtleCrypto primitives for operations.
 * @param keyStore specific keyStore that securely holds keys.
 */
class CryptoOperations(subtleCrypto: SubtleCrypto, val keyStore: IKeyStore, private val logger: ILogger = ConsoleLogger()) {
    val subtleCryptoFactory = SubtleCryptoFactory(subtleCrypto, logger)

    /**
     * Sign payload with key stored in keyStore.
     * @param payload to sign.
     * @param signingKeyReference reference to key stored in keystore.
     */
    fun sign(payload: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null): ByteArray {
        val privateKey = keyStore.getPrivateKey(signingKeyReference)
        val alg = algorithm ?: privateKey.alg ?: throw Error("No Algorithm specified for key $signingKeyReference")
        val subtle = subtleCryptoFactory.getMessageSigner(alg.name, SubtleCryptoScope.Private)
        val key = subtle.importKey(KeyFormat.Jwk, privateKey.getKey().toJWK(), alg, false, listOf(KeyUsage.Sign))
        return subtle.sign(alg, key, payload)
    }

    /**
     * Verify payload with key stored in keyStore.
     */
    fun verify(payload: ByteArray, signature: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null) {
        val publicKey = keyStore.getPublicKey(signingKeyReference)
        val alg = algorithm ?: publicKey.alg ?: throw Error("No Algorithm specified for key $signingKeyReference")
        val subtle = subtleCryptoFactory.getMessageSigner(alg.name, SubtleCryptoScope.Public)
        val key = subtle.importKey(KeyFormat.Jwk, publicKey.getKey().toJWK(), alg, true, listOf(KeyUsage.Verify))
        if (!subtle.verify(alg, key, signature, payload)) {
            throw Error("Signature invalid")
        }
    }

    /**
     * Encrypt payload with key stored in keyStore.
     */
    fun encrypt() {
        TODO("Not implemented")
    }

    /**
     * Decrypt payload with key stored in keyStore.
     */
    fun decrypt() {
        TODO("Not implemented")
    }

    /**
     * Generates a key pair.
     * @param keyType the type of key to generate
     * @returns the associated public key
     */
    fun generateKeyPair(keyReference: String, keyType: KeyType): PublicKey {
        when (keyType) {
            KeyType.Octets -> throw Error("Cannot generate a symmetric key")
            KeyType.RSA -> {
                val subtle = subtleCryptoFactory.getSharedKeyEncrypter(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, SubtleCryptoScope.Private)
                val keyPair = subtle.generateKeyPair(RsaHashedKeyAlgorithm(
                    modulusLength = 4096UL,
                    publicExponent = 65537UL,
                    hash = Sha.Sha256,
                    additionalParams = mapOf(
                        "KeyReference" to keyReference
                    )
                ), false, listOf(KeyUsage.Encrypt, KeyUsage.Decrypt))
                keyStore.save(keyReference, RsaPrivateKey(subtle.exportKeyJwk(keyPair.privateKey)))
            }
            KeyType.EllipticCurve -> {
                val subtle = subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.Private)
                val keyPair = subtle.generateKeyPair(EcKeyGenParams(
                    namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                    additionalParams = mapOf(
                        "hash" to Sha.Sha256,
                        "KeyReference" to keyReference
                    )
                ), true, listOf(KeyUsage.Sign, KeyUsage.Verify))
                keyStore.save(keyReference, EllipticCurvePrivateKey(subtle.exportKeyJwk(keyPair.privateKey)))
            }
        }
        return keyStore.getPublicKey(keyReference).getKey()
    }

    /**
     * Generate a pairwise key.
     * @param seed to be used to create pairwise key.
     *
     */
    fun generatePairwise(seed: String) {
        TODO("Not implemented")
    }

    /**
     * Generate a seed.
     */
    fun generateSeed(): String {
        TODO("Not implemented")
    }
}