/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.KeyTypeFactory
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.models.AndroidConstants
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.EcKeyGenParams
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaHashedKeyAlgorithm
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoFactory
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import com.microsoft.did.sdk.util.controlflow.SignatureException
import com.microsoft.did.sdk.util.log.SdkLog
import java.security.SecureRandom

/**
 * Class that encompasses all of Crypto
 * @param subtleCrypto primitives for operations.
 * @param keyStore specific keyStore that securely holds keys.
 */
class CryptoOperations(
    subtleCrypto: SubtleCrypto,
    val keyStore: KeyStore,
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
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
        val alg = algorithm ?: privateKey.alg ?: throw KeyException("No Algorithm specified for key $signingKeyReference")
        val subtle = subtleCryptoFactory.getMessageSigner(alg.name, SubtleCryptoScope.PRIVATE)
        val key = subtle.importKey(KeyFormat.Jwk, privateKey.getKey().toJWK(), alg, false, listOf(KeyUsage.Sign))
        return subtle.sign(alg, key, payload)
    }

    /**
     * Verify payload with key stored in keyStore.
     */
    fun verify(payload: ByteArray, signature: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null) {
        SdkLog.d("Verifying with $signingKeyReference")
        val publicKey = keyStore.getPublicKey(signingKeyReference)
        val alg = algorithm ?: publicKey.alg ?: throw KeyException("No Algorithm specified for key $signingKeyReference")
        val subtle = subtleCryptoFactory.getMessageSigner(alg.name, SubtleCryptoScope.PUBLIC)
        val key = subtle.importKey(KeyFormat.Jwk, publicKey.getKey().toJWK(), alg, true, listOf(KeyUsage.Verify))
        if (!subtle.verify(alg, key, signature, payload)) {
            throw SignatureException("Signature invalid")
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
            KeyType.Octets -> throw KeyException("Cannot generate a symmetric key")
            KeyType.RSA -> {
                val subtle = subtleCryptoFactory.getSharedKeyEncrypter(
                    W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
                    SubtleCryptoScope.PRIVATE
                )
                val keyPair = subtle.generateKeyPair(
                    RsaHashedKeyAlgorithm(
                        modulusLength = 4096UL,
                        publicExponent = 65537UL,
                        hash = Sha.SHA256.algorithm,
                        additionalParams = mapOf("KeyReference" to keyReference)
                    ), false, listOf(KeyUsage.Encrypt, KeyUsage.Decrypt)
                )
                SdkLog.d("Saving key pair to keystore.")
                keyStore.save(keyReference, RsaPrivateKey(subtle.exportKeyJwk(keyPair.privateKey)))
            }
            KeyType.EllipticCurve -> {
                val subtle = subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.PRIVATE)
                val keyPair = subtle.generateKeyPair(
                    EcKeyGenParams(
                        namedCurve = W3cCryptoApiConstants.Secp256k1.value,
                        additionalParams = mapOf(
                            "hash" to Sha.SHA256.algorithm,
                            "KeyReference" to keyReference
                        )
                    ), true, listOf(KeyUsage.Sign, KeyUsage.Verify)
                )
                SdkLog.d("Saving key pair to keystore.")
                keyStore.save(keyReference, EllipticCurvePrivateKey(subtle.exportKeyJwk(keyPair.privateKey)))
            }
        }
        return keyStore.getPublicKey(keyReference).getKey()
    }

    /**
     * Generate a pairwise key for the specified algorithms
     * @param algorithm for the key
     * @param seedReference Reference to the seed
     * @param userDid Id for the user
     * @param peerId Id for the peer
     * @returns the pairwise private key
     */
    fun generatePairwise(algorithm: Algorithm, seedReference: String, userDid: String, peerId: String): PrivateKey {
        val masterKey: ByteArray = this.generatePersonaMasterKey(seedReference, userDid)

        return when (val keyType = KeyTypeFactory.createViaWebCrypto(algorithm)) {
            KeyType.EllipticCurve -> ellipticCurvePairwiseKey.generate(this, masterKey, algorithm, peerId)
            else -> throw PairwiseKeyException("Pairwise key for type '${keyType.value}' is not supported.")
        }
    }

    /**
     * Generates a 256 bit seed.
     */
    fun generateAndStoreSeed() {
        val randomNumberGenerator = SecureRandom()
        val seed = randomNumberGenerator.generateSeed(16)
        val secretKey = SecretKey(JsonWebKey(k = Base64Url.encode(seed)))
        keyStore.save(AndroidConstants.masterSeed.value, secretKey)
    }

    /**
     * Generate a pairwise master key.
     * @param seedReference  The master seed for generating pairwise keys
     * @param userDid  The owner DID
     * @returns the master key for the user
     */
    fun generatePersonaMasterKey(seedReference: String, userDid: String): ByteArray {
        // Set of master keys for the different persona's
        val masterKeys: MutableMap<String, ByteArray> = mutableMapOf()

        var masterKey: ByteArray? = masterKeys[userDid]
        if (masterKey != null)
            return masterKey

        // Get the seed
        val jwk = keyStore.getSecretKey(seedReference)

        masterKey = generateMasterKeyFromSeed(jwk, userDid)
        masterKeys[userDid] = masterKey
        return masterKey
    }

    private fun generateMasterKeyFromSeed(jwk: KeyContainer<SecretKey>, userDid: String): ByteArray {
        // Get the subtle crypto
        val crypto: SubtleCrypto =
            subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.PRIVATE)

        // Generate the master key
        val alg = Algorithm(name = W3cCryptoApiConstants.HmacSha512.value)
        val masterJwk = JsonWebKey(
            kty = KeyType.Octets.value,
            alg = JoseConstants.Hs512.value,
            k = jwk.getKey().k
        )
        val key = crypto.importKey(
            KeyFormat.Jwk, masterJwk, alg, false, listOf(
                KeyUsage.Sign
            )
        )
        return crypto.sign(alg, key, userDid.map { it.toByte() }.toByteArray())
    }
}