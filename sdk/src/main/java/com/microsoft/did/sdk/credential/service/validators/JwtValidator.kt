/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.ValidatorException
import java.security.PublicKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that can be used to validate JwsTokens.
 */
@Singleton
class JwtValidator @Inject constructor(
    private val resolver: Resolver
) {

    /**
     * Verify the signature on the JwsToken.
     */
    suspend fun verifySignature(token: JwsToken): Boolean {
        val didInHeader = getDidFromHeader(token)
        val publicKeys = resolvePublicKeys(didInHeader)
        return token.verify(publicKeys)
    }

    fun validateDidInHeaderAndPayload(jwsToken: JwsToken, didInPayload: String): Boolean {
        val didInHeader = getDidFromHeader(jwsToken)
        return didInHeader == didInPayload
    }

    private fun getKid(token: JwsToken): Pair<String, String> {
        token.getKeyId()?.let {
            kid ->
            val parsedKid = kid.split("#")
            return Pair(parsedKid[0], parsedKid[1])
        }
        throw ValidatorException("JWS contains no key id")
    }

    private suspend fun resolvePublicKeys(did: String): List<PublicKey> {
        return when (val requesterDidDocument = resolver.resolve(did)) {
            is Result.Success -> {
                val publicKeys = requesterDidDocument.payload.verificationMethod
                if (publicKeys.isNullOrEmpty()) throw ValidatorException("No public key found in identifier document")
                publicKeys.map { it.toPublicKey() }
            }
            is Result.Failure -> throw ValidatorException("Unable to fetch public keys", requesterDidDocument.payload)
        }
    }

    private fun getDidFromHeader(token: JwsToken): String {
        val (didInHeader, _) = getKid(token)
        return didInHeader
    }
}