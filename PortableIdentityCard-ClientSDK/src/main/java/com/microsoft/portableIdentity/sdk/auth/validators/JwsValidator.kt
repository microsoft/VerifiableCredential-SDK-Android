/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger

/**
 * Class that can be used to validate JwsTokens.
 */
object JwsValidator {

    /**
     * Verify the signature on the JwsToken.
     */
    suspend fun verifySignature(token: JwsToken): Boolean {
        val headers = token.signatures.first().header ?: throw Exception("JwsToken is not signed")
        val cryptoOperations = DidSdkConfig.identityManager.cryptoOperations
        val publicKeys = resolvePublicKeys(headers, cryptoOperations)
        return token.verify(cryptoOperations, publicKeys)
    }

    private suspend fun resolvePublicKeys(tokenHeaders: Map<String, String>, cryptoOperations: CryptoOperations): List<PublicKey> {
        val requesterDid = tokenHeaders["kid"] ?: throw Exception("No kid specified in header")
        val requesterDidDocument = DidSdkConfig.identityManager.resolver.resolve(requesterDid, cryptoOperations)
        return requesterDidDocument.document.publicKeys.map {
            it.toPublicKey(BaseLogger)
        }
    }
}