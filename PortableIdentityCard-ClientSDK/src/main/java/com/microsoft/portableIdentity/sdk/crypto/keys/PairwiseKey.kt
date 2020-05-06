/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
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
        // val personaMasterKey: ByteArray = this.generatePersonaMasterKey(seedReference, personaId);

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
     * TODO(Deleting Logan's translated code from Typescript SDK for now)
     */
    private fun generatePersonaMasterKey (seedReference: String, personaId: String) {}
}