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
import com.nimbusds.jose.jwk.JWK
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
        val (didInHeader: String?, keyIdInHeader: String) = getDidAndKeyIdFromHeader(token)
        if (didInHeader == null) throw ValidatorException("JWS contains no DID")
        val publicKeyJwks = resolvePublicKeyJwks(didInHeader, keyIdInHeader)
        return verifySignatureUsingPublicKey(token, publicKeyJwks)
    }

    private fun verifySignatureUsingPublicKey(token: JwsToken, publicKeys: List<JWK>): Boolean {
        return token.verify(publicKeys)
    }

    fun validateDidInHeaderAndPayload(jwsToken: JwsToken, didInPayload: String): Boolean {
        val didInHeader = getDidAndKeyIdFromHeader(jwsToken).first ?: throw ValidatorException("JWS contains no DID")
        return didInHeader == didInPayload
    }

    private fun getDidAndKeyIdFromHeader(token: JwsToken): Pair<String?, String> {
        token.keyId?.let { kid -> return JwaCryptoHelper.extractDidAndKeyId(kid) }
        throw ValidatorException("JWS contains no key id")
    }

    private suspend fun resolvePublicKeyJwks(did: String, keyId: String): List<JWK> {
        return when (val requesterDidDocument = resolver.resolve(did)) {
            is Result.Success -> {
                val publicKeys = requesterDidDocument.payload.verificationMethod
                if (publicKeys.isNullOrEmpty()) throw ValidatorException("No public key found in identifier document")
                publicKeys.filter { publicKey -> JwaCryptoHelper.extractDidAndKeyId(publicKey.id).second == keyId }.map { it.publicKeyJwk }
            }
            is Result.Failure -> throw ValidatorException("Unable to fetch public keys", requesterDidDocument.payload)
        }
    }
}