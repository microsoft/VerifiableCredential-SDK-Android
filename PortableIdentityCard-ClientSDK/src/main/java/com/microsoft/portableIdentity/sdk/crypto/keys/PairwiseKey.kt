/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseConstants

/**
 * Class to model pairwise keys
 */
class PairwiseKey(private val crypto: CryptoOperations) {

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

        return when (val keyType = KeyTypeFactory.createViaWebCrypto(algorithm)) {
            KeyType.EllipticCurve -> EllipticCurvePairwiseKey.generate(this.crypto, personaMasterKey, algorithm as EcKeyGenParams, peerId);
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
        val jwk = this.crypto.keyStore.getSecretKey(seedReference)

        // Get the subtle crypto
        val crypto: SubtleCrypto = this.crypto.subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private);

        // Generate the master key
        val alg: Algorithm =
            EcdsaParams(
                hash = Sha.Sha512
            )
        val masterJwk = JsonWebKey(
                kty = KeyType.Octets.value,
                alg = JoseConstants.Hs512.value,
                k = jwk.getKey().k
            )
        val key = crypto.importKey(
            KeyFormat.Jwk, masterJwk, alg, false, listOf(
                KeyUsage.Sign));
        val masterKey = crypto.sign(alg, key, personaId.map { it.toByte() }.toByteArray());
        this.masterKeys[personaId] = masterKey;
        return masterKey;
    }
}