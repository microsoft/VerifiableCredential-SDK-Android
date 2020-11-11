/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsSignature
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.ValidatorException
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that can be used to validate JwsTokens.
 */
@Singleton
class JwtValidator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val resolver: Resolver,
    private val serializer: Json
) {

    /**
     * Verify the signature on the JwsToken.
     */
    suspend fun verifySignature(token: JwsToken): Boolean {
        val didInHeader = getDidFromHeader(token)
        val publicKeys = resolvePublicKeys(didInHeader)
        return token.verify(cryptoOperations, publicKeys)
    }

    fun validateDidInHeaderAndPayload(jwsToken: JwsToken, didInPayload: String): Boolean {
        val didInHeader = getDidFromHeader(jwsToken)
        return didInHeader == didInPayload
    }

    private fun getKid(signature: JwsSignature): Pair<String, String> {
        val kid = signature.getKid(serializer) ?: throw ValidatorException("no kid specified in token")
        val parsedKid = kid.split("#")
        return Pair(parsedKid[0], parsedKid[1])
    }

    private suspend fun resolvePublicKeys(did: String): List<PublicKey> {
        return when (val requesterDidDocument = resolver.resolve(did)) {
            is Result.Success -> {
                val publicKeys = requesterDidDocument.payload.publicKey.map { it.toPublicKey() }
                publicKeys
            }
            is Result.Failure -> throw ValidatorException("Unable to fetch public keys", requesterDidDocument.payload)
        }
    }

    private fun getDidFromHeader(token: JwsToken): String {
        val signature = token.signatures.first()
        val (didInHeader, _) = getKid(signature)
        return didInHeader
    }
}