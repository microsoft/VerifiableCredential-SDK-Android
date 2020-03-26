/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.protectors.Signer
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer

/**
 * OIDC Response formed from a Request.
 *
 * @param request that response is responding to.
 * @param signer optional parameter used to protect the response.
 */
class OidcResponse(request: OidcRequest): Response {

    /**
     * list of collected credentials to be sent in response.
     */
    override val collectedCredentials: MutableList<Credential> = mutableListOf()

    /**
     * Add Credential to be put into response.
     *
     * @param credential to be added to response.
     */
    override fun addCredential(credential: Credential) {
        collectedCredentials.add(credential)
    }

    fun formResponse(collectedCredentials: List<Credential>): String {
        var responseBody: String
        val responseContent = createResponseContent(collectedCredentials)
        val serializedResponseContent = Serializer.stringify(OidcResponseContent.serializer(), responseContent)
        val protectedToken: JwsToken = Signer.sign(serializedResponseContent)
        val serializedToken = protectedToken.serialize(JwsFormat.Compact)
        responseBody = "id_token=${serializedToken}"
        if (!responseContent.state.isNullOrBlank()) {
            responseBody += "&state=${responseContent.state}"
        }
        return responseBody
    }

    /**
     * Create Response Content object from collectedCredentials and Request Contents.
     */
    private fun createResponseContent(collectedCredentials: List<Credential>): OidcResponseContent {
        TODO("implement when protocol is finalized")
    }
}