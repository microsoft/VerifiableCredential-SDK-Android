/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.models.oidc.RevocationResponseClaims
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that forms Response Contents Properly.
 */
@Singleton
class RevocationResponseFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Serializer,
    private val signer: TokenSigner
) {

    fun formatResponse(revocationRequest: RevocationRequest, expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responder = revocationRequest.owner
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val responseId = UUID.randomUUID().toString()
        val contents =
            RevocationResponseClaims(revocationRequest.rpList, revocationRequest.reason, revocationRequest.verifiableCredential.raw).apply {
                publicKeyThumbPrint = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
                audience = revocationRequest.audience
                did = responder.id
                publicKeyJwk = key.toJWK()
                responseCreationTime = issuedTime
                responseExpirationTime = expiryTime
                this.responseId = responseId
            }
        return signContents(contents, responder)
    }

    private fun signContents(contents: RevocationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(RevocationResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}