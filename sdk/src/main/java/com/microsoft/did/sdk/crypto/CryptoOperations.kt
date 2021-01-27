/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.KeyTypeFactory
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyFormat
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.provider.Provider
import com.microsoft.did.sdk.crypto.provider.Secp256k1Provider
import com.microsoft.did.sdk.util.controlflow.PairwiseKeyException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.SecretKey

class CryptoOperations(
    private val keyStore: KeyStore,
    private val defaultProvider: Provider = Secp256k1Provider(),
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
) {

    fun sign(payload: ByteArray, keyId: String, provider: Provider = defaultProvider): ByteArray {
        val signingKey = keyStore.getKey<PrivateKey>(keyId)
        return provider.sign(signingKey, payload)
    }

    fun verify(payload: ByteArray, signature: ByteArray, keyId: String, provider: Provider = defaultProvider): Boolean {
        val publicKey = keyStore.getKey<PublicKey>(keyId)
        return provider.verify(publicKey, payload, signature)
    }

    fun encrypt(payload: ByteArray, keyId: String, provider: Provider = defaultProvider): ByteArray {
        val secretKey = keyStore.getKey<SecretKey>(keyId)
        return provider.encrypt(secretKey, payload)
    }

    fun decrypt(payload: ByteArray, keyId: String, provider: Provider = defaultProvider): ByteArray {
        val secretKey = keyStore.getKey<SecretKey>(keyId)
        return provider.decrypt(secretKey, payload)
    }

    /**
     * Generates a KeyPair with the given provider.
     *
     * Stores the private key in the KeyStore with given keyId and returns the corresponding public key.
     */
    fun generateKeyPair(keyId: String, provider: Provider = defaultProvider): PublicKey {
        val keyPair = provider.generateKeyPair()
        keyStore.saveKey(keyPair.private, keyId)
        return keyPair.public
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
        keyStore.saveKey(AndroidConstants.masterSeed.value, secretKey)
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

        keyStore.getKey<SecretKey>(seedReference)
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