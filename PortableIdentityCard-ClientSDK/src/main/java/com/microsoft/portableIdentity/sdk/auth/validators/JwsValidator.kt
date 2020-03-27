/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that can be used to validate JwsTokens.
 */
@Singleton
class JwsValidator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val resolver: IResolver
    ) {

    /**
     * Verify the signature on the JwsToken.
     */
    suspend fun verifySignature(token: JwsToken): Boolean {
        val headers = token.signatures.first().header ?: throw Exception("JwsToken is not signed")
        val cryptoOperations = cryptoOperations
        val publicKeys = resolvePublicKeys(headers, cryptoOperations)
        return token.verify(cryptoOperations, publicKeys)
    }

    private suspend fun resolvePublicKeys(tokenHeaders: Map<String, String>, cryptoOperations: CryptoOperations): List<PublicKey> {
        val requesterDid = tokenHeaders["kid"] ?: throw Exception("No kid specified in header")
        val requesterDidDocument = resolver.resolve(requesterDid, cryptoOperations)
        return requesterDidDocument.document.publicKeys.map {
            it.toPublicKey()
        }
    }
}