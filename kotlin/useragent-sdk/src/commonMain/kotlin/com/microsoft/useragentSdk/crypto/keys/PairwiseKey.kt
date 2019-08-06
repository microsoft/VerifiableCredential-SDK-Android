/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.useragentSdk.crypto.keys

import com.microsoft.useragentSdk.crypto.models.*
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
    private var masterKeys: Map<String, ByteArray> = emptyMap()


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
        switch (keyType) {
            case KeyType.EC:
            return EcPairwiseKey.generate(this.cryptoFactory, personaMasterKey, <EcKeyGenParams>algorithm, peerId);
            case KeyType.RSA:
            return RsaPairwiseKey.generate(this.cryptoFactory, personaMasterKey, <RsaHashedKeyGenParams>algorithm, peerId);

            default:
            throw new CryptoError(algorithm, `Pairwise key for type '${keyType}' is not supported.`);
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
        val alg: AlgorithmIdentifier = AlgorithmIdentifier(
            algorithm = EcdsaParams(W3cCryptoApiConstants.Hmac.value, AlgorithmIdentifier(
                W3cCryptoApiConstants.Sha512.value, null))
        )
        val masterJwk: KeyData = KeyData(
            JsonWebKey(
                kty = KeyType.Octet.value,
                alg = JoseConstants.Hs512.value,
                k = jwk.k
                ),
            null
        )
        val key = crypto.importKey(KeyFormat.Jwk, masterJwk, alg, false, listOf(KeyUsage.Sign));
        const masterKey = await crypto.sign(alg, key, Buffer.from(personaId));
        mk = Buffer.from(masterKey);
        this.masterKeys.set(personaId, mk);
        return mk;
    }
}