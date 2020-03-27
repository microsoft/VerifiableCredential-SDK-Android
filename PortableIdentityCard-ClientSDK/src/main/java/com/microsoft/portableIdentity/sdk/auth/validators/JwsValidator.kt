/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsSignature
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import java.lang.Exception

/**
 * Class that can be used to validate JwsTokens.
 */
object JwsValidator {

    /**
     * Verify the signature on the JwsToken.
     */
    suspend fun verifySignature(token: JwsToken): Boolean {
        val signature = token.signatures.first()
        val (did, _) = getKid(signature)
        val cryptoOperations = DidSdkConfig.identityManager.cryptoOperations
        val publicKeys = resolvePublicKeys(did, cryptoOperations)
        return token.verify(cryptoOperations, publicKeys)
    }

    private fun getKid(signature: JwsSignature): Pair<String, String> {
        val kid = signature.getKid(BaseLogger) ?: throw Exception("no kid specified in token")
        val parsedKid = kid.split("#")
        return Pair(parsedKid[0], parsedKid[1])
    }

    private suspend fun resolvePublicKeys(kid: String, cryptoOperations: CryptoOperations): List<PublicKey> {
        val requesterDidDocument = DidSdkConfig.identityManager.resolver.resolve(kid, cryptoOperations)
        return requesterDidDocument.document.publicKeys.map {
            it.toPublicKey(BaseLogger)
        }
    }
}