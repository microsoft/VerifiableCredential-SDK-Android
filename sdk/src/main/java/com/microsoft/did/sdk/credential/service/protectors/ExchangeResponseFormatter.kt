/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.models.oidc.ExchangeResponseClaims
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeResponseFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Json,
    private val signer: TokenSigner
) {
    fun formatResponse(exchangeRequest: ExchangeRequest, expiryInSeconds: Int): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responseId = UUID.randomUUID().toString()
        return createAndSignOidcResponseContent(exchangeRequest, issuedTime, expiryTime, responseId)
    }

    private fun createAndSignOidcResponseContent(
        exchangeRequest: ExchangeRequest,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String
    ): String {
        val requester = exchangeRequest.requester
        val key = cryptoOperations.keyStore.getPublicKey(requester.signatureKeyReference).getKey()
        val contents = ExchangeResponseClaims(exchangeRequest.verifiableCredential.raw, exchangeRequest.pairwiseDid).apply {
            publicKeyThumbPrint = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            audience = exchangeRequest.audience
            did = requester.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
            this.responseId = responseId
        }
        return signContents(contents, requester)
    }

    private fun signContents(contents: ExchangeResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.encodeToString(ExchangeResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}