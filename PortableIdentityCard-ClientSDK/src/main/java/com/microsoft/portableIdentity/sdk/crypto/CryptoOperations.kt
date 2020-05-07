// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.crypto

import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PairwiseKey
import com.microsoft.portableIdentity.sdk.crypto.keys.PrivateKey
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.keys.SecretKey
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoFactory
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import java.security.SecureRandom

/**
 * Class that encompasses all of Crypto
 * @param subtleCrypto primitives for operations.
 * @param keyStore specific keyStore that securely holds keys.
 */
class CryptoOperations (
    subtleCrypto: SubtleCrypto,
    val keyStore: KeyStore
) {
    val subtleCryptoFactory = SubtleCryptoFactory(subtleCrypto)

    /**
     * Sign payload with key stored in keyStore.
     * @param payload to sign.
     * @param signingKeyReference reference to key stored in keystore.
     */
    fun sign(payload: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null): ByteArray {
        SdkLog.d("Signing with $signingKeyReference")
        val privateKey = keyStore.getPrivateKey(signingKeyReference)
        val alg = algorithm ?: privateKey.alg ?: throw SdkLog.error("No Algorithm specified for key $signingKeyReference")
        val subtle = subtleCryptoFactory.getMessageSigner(alg.name, SubtleCryptoScope.Private)
        val key = subtle.importKey(KeyFormat.Jwk, privateKey.getKey().toJWK(), alg, false, listOf(KeyUsage.Sign))
        return subtle.sign(alg, key, payload)
    }

    /**
     * Verify payload with key stored in keyStore.
     */
    fun verify(payload: ByteArray, signature: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null) {
        SdkLog.d("Verifying with $signingKeyReference")
        val publicKey = keyStore.getPublicKey(signingKeyReference)
        val alg =
            algorithm ?: publicKey.alg ?: throw SdkLog.error("No Algorithm specified for key $signingKeyReference")
        val subtle = subtleCryptoFactory.getMessageSigner(alg.name, SubtleCryptoScope.Public)
        val key = subtle.importKey(KeyFormat.Jwk, publicKey.getKey().toJWK(), alg, true, listOf(KeyUsage.Verify))
        if (!subtle.verify(alg, key, signature, payload)) {
            throw SdkLog.error("Signature invalid")
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
        SdkLog.d("Generating new key pair $keyReference of type ${keyType.value}")
        when (keyType) {
            KeyType.Octets -> throw SdkLog.error("Cannot generate a symmetric key")
            KeyType.RSA -> {
                val subtle = subtleCryptoFactory.getSharedKeyEncrypter(
                    W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
                    SubtleCryptoScope.Private
                )
                val keyPair = subtle.generateKeyPair(
                    RsaHashedKeyAlgorithm(
                        modulusLength = 4096UL,
                        publicExponent = 65537UL,
                        hash = Sha.Sha256,
                        additionalParams = mapOf("KeyReference" to keyReference)
                    ), false, listOf(KeyUsage.Encrypt, KeyUsage.Decrypt)
                )
                SdkLog.d("Saving key pair to keystore.")
                keyStore.save(keyReference, RsaPrivateKey(subtle.exportKeyJwk(keyPair.privateKey)))
            }
            KeyType.EllipticCurve -> {
                val subtle =
                    subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.Private)
                val keyPair = subtle.generateKeyPair(
                    EcKeyGenParams(
                        namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                        additionalParams = mapOf(
                            "hash" to Sha.Sha256,
                            "KeyReference" to keyReference
                        )
                    ), true, listOf(KeyUsage.Sign, KeyUsage.Verify)
                )
                SdkLog.d("Saving key pair to keystore.")
                keyStore.save(
                    keyReference,
                    EllipticCurvePrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
                )
            }
        }
        return keyStore.getPublicKey(keyReference).getKey()
    }

    /**
     * Generate a pairwise key.
     * @param seed to be used to create pairwise key.
     *
     */
    fun generatePairwise(algorithm: EcKeyGenParams, seed: String, personaId: String, peerId: String): PrivateKey {
//        TODO("Not implemented")
        val pairwiseKey = PairwiseKey(this)
        return pairwiseKey.generatePairwiseKey(algorithm, seed, personaId, peerId)
    }

    /**
     * Generates a 256 bit seed.
     */
    fun generateAndStoreSeed() {
        val randomNumberGenerator = SecureRandom()
        val seed = randomNumberGenerator.generateSeed(32)
        val secretKey = SecretKey(JsonWebKey(k=Base64Url.encode(seed)))
        keyStore.save("seed", secretKey)
    }
}