/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoHelper
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
        val (didInHeader: String?, keyIdInHeader: String) = token.keyId?.let { JwaCryptoHelper.extractDidAndKeyId(it) } ?: throw ValidatorException("key id is missing in jwt")
        val publicKeys = didInHeader?.let { retrievePublicKey(didInHeader, keyIdInHeader) } ?: throw ValidatorException("DID is missing in jwt header")
        if (publicKeys.isNullOrEmpty()) throw ValidatorException("No public key found in identifier document")
        return token.verify(publicKeys)
    }

    fun validateDidInHeaderAndPayload(jwsToken: JwsToken, didInPayload: String): Boolean {
        val didInHeader = getDidFromHeader(jwsToken)
        didInHeader ?: throw ValidatorException("JWS contains no DID")
        return didInHeader == didInPayload
    }

    private fun getDidAndKeyIdFromHeader(token: JwsToken): Pair<String, String> {
        token.keyId?.let { kid ->
            val parsedKid = kid.split("#")
            return Pair(parsedKid[0], parsedKid[1])
        }
        throw ValidatorException("JWS contains no key id")
    }

    private suspend fun retrievePublicKey(did: String, keyId: String): List<PublicKey> {
        return when (val requesterDidDocument = resolver.resolve(did)) {
            is Result.Success -> {
                val publicKeys = requesterDidDocument.payload.verificationMethod
                if (publicKeys.isNullOrEmpty()) throw ValidatorException("No public key found in identifier document")
                publicKeys.filter { JwaCryptoHelper.extractDidAndKeyId(it.id).second == keyId }.map { it.toPublicKey() }
            }
            is Result.Failure -> throw ValidatorException("Unable to fetch public keys", requesterDidDocument.payload)
        }
    }

    private fun getDidFromHeader(token: JwsToken): String? {
        token.keyId ?: throw ValidatorException("JWS contains no key id")
        val (didInHeader, _) = JwaCryptoHelper.extractDidAndKeyId(token.keyId!!)
        return didInHeader
    }
}