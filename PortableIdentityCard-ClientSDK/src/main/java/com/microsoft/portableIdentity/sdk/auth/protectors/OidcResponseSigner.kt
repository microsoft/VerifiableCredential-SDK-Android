/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.AuthenticationConstants.RESPONSE_EXPIRATION_IN_MINUTES
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.floor

/**
 * Class that can protect some content by signing.
 */
@Singleton
class OidcResponseSigner @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    @Named("signatureKeyReference") private val signatureKeyReference: String
) {

    /**
     * Sign content with keyReference.
     * @return JwsToken
     */
    fun sign(response: OidcResponse, responder: Identifier, keyReference: String = signatureKeyReference, additionalHeaders: Map<String, String> = emptyMap()): JwsToken {
        val responseContent = formResponseContents(response, responder.document.id, keyReference)
        val serializedResponseContent = Serializer.stringify(OidcResponseContent.serializer(), responseContent)
        val token = JwsToken(serializedResponseContent)
        token.sign(keyReference, cryptoOperations, additionalHeaders)
        return token
    }

    private fun formResponseContents(response: OidcResponse,responderDid: String, useKey: String = signatureKeyReference, expiresIn: Int = RESPONSE_EXPIRATION_IN_MINUTES): OidcResponseContent {
        val requestContent = response.getRequestContents()
        val (iat, exp) = createIatAndExp(expiresIn)
        val key = cryptoOperations.keyStore.getPublicKey(useKey).getKey()

        return OidcResponseContent(
            sub = key.getThumbprint(cryptoOperations, Sha.Sha256),
            aud = requestContent.redirectUrl,
            nonce = requestContent.nonce,
            did = responderDid,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            state = requestContent.state,
            attestations = ""
        )
    }

    private fun createIatAndExp(expiresIn: Int = RESPONSE_EXPIRATION_IN_MINUTES): Pair<Long, Long> {
        val currentTime = Date().time
        val expiration = currentTime + 1000 * 60 * expiresIn
        val exp = floor(expiration / 1000f).toLong()
        val iat = floor( currentTime / 1000f).toLong()
        return Pair(iat, exp)
    }
}