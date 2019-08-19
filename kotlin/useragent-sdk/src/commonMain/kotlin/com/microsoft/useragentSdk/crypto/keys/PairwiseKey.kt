/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.useragentSdk.crypto.keys

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.*
import com.microsoft.useragentSdk.crypto.plugins.CryptoFactory
import com.microsoft.useragentSdk.crypto.protocols.jose.JoseConstants

/**
 * Class to model pairwise keys
 */
class PairwiseKey(cryptoFactory: CryptoFactory) {

    /**
     * Get or set the crypto factory to use, containing the crypto suite and the key store.
     */
    private var cryptoFactory: CryptoFactory = cryptoFactory

    // Set of master keys for the different persona's
    private var masterKeys: MutableMap<String, ByteArray> = mutableMapOf()


    /**
     * Generate a pairwise key for the specified algorithms
     * @param algorithm for the key
     * @param seedReference Reference to the seed
     * @param personaId Id for the persona
     * @param peerId Id for the peer
     */
    fun generatePairwiseKey(algorithm: Algorithm, seedReference: String, personaId: String, peerId: String): PrivateKey {
        val personaMasterKey: ByteArray = this.generatePersonaMasterKey(seedReference, personaId);

        val keyType = KeyTypeFactory.createViaWebCrypto(algorithm);
        return when (keyType) {
//            KeyType.EllipticCurve -> EcPairwiseKey.generate(this.cryptoFactory, personaMasterKey, <EcKeyGenParams>algorithm, peerId);
//            KeyType.RSA -> RsaPairwiseKey.generate(this.cryptoFactory, personaMasterKey, <RsaHashedKeyGenParams>algorithm, peerId);
            else -> error("Pairwise key for type '${keyType.value}' is not supported.");
        }
    }

    /**
     * Generate a pairwise master key.
     * @param seedReference  The master seed for generating pairwise keys
     * @param personaId  The owner DID
     */
    private fun generatePersonaMasterKey (seedReference: String, personaId: String): ByteArray {
        var mk: ByteArray? = this.masterKeys[personaId];

        if (mk != null) {
            return mk;
        }

        // Get the seed
        val jwk = this.cryptoFactory.keyStore.getSecretKey(seedReference, false);

        // Get the subtle crypto
        val crypto: SubtleCrypto = this.cryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value);

        // Generate the master key
        val alg: Algorithm =
            EcdsaParams(
                name = W3cCryptoApiConstants.Hmac.value,
                hash = Algorithm(
                    W3cCryptoApiConstants.Sha512.value
                )
            )
        val masterJwk: KeyData =
            KeyData(
                jwk = JsonWebKey(
                    kty = KeyType.Octets.value,
                    alg = JoseConstants.Hs512.value,
                    k = jwk.k
                )
            )
        val key = crypto.importKey(
            KeyFormat.Jwk, masterJwk, alg, false, listOf(
                KeyUsage.Sign));
        val masterKey = crypto.sign(alg, key, personaId.map { it.toByte() }.toByteArray());
        this.masterKeys[personaId] = masterKey;
        return masterKey;
    }
}