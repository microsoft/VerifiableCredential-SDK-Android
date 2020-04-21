/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsSignature
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ValidatorException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that can be used to validate JwsTokens.
 */
@Singleton
class JwsValidator @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val resolver: Resolver
) {

    /**
     * Verify the signature on the JwsToken.
     */
    suspend fun verifySignature(token: JwsToken): Result<Boolean> {
        return try {
            val signature = token.signatures.first()
            val (did, _) = getKid(signature)
            return when(val publicKeys = resolvePublicKeys(did)) {
                is Result.Success -> {
                    val isValid = token.verify(cryptoOperations, publicKeys.payload)
                    Result.Success(isValid)
                }
                is Result.Failure -> throw publicKeys.payload
            }
        } catch (exception: Exception) {
            val cryptoException = ValidatorException("Unable to validate signature", exception)
            Result.Failure(cryptoException)
        }
    }

    private fun getKid(signature: JwsSignature): Pair<String, String> {
        val kid = signature.getKid() ?: throw Exception("no kid specified in token")
        val parsedKid = kid.split("#")
        return Pair(parsedKid[0], parsedKid[1])
    }

    //TODO: Test this when key format is changed from hex to jwk
    private suspend fun resolvePublicKeys(did: String): Result<List<PublicKey>> {
        return when (val requesterDidDocument = resolver.resolve(did)) {
            is Result.Success -> {
                val publicKeys = requesterDidDocument.payload.publicKey.map { it.toPublicKey() }
                Result.Success(publicKeys)
            }
            is Result.Failure -> requesterDidDocument
        }
    }
}