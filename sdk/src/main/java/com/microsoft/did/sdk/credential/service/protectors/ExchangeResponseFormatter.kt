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
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeResponseFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Serializer,
    private val signer: TokenSigner
) : OidcResponseFormatter {
    fun formatExchangeResponse(exchangeRequest: ExchangeRequest, expiryInSeconds: Int): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        val jti = UUID.randomUUID().toString()
        return createAndSignOidcResponseContentForExchange(exchangeRequest, iat, exp, jti)
    }

    private fun createAndSignOidcResponseContentForExchange(
        exchangeRequest: ExchangeRequest,
        issuedTime: Long,
        expiryTime: Long,
        jti: String
    ): String {
        val requester = exchangeRequest.requester
        val key = cryptoOperations.keyStore.getPublicKey(requester.signatureKeyReference).getKey()
        val contents = ExchangeResponseClaims(exchangeRequest.verifiableCredential?.raw, exchangeRequest.pairwiseDid).apply {
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            aud = exchangeRequest.audience
            did = requester.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            expirationTime = expiryTime
            responseId = jti
        }
        return signContentsForExchange(contents, requester)
    }

    private fun signContentsForExchange(contents: ExchangeResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(ExchangeResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}