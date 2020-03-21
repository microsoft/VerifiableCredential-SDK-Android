/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OIDCResponseContent
import com.microsoft.portableIdentity.sdk.auth.protectors.Signer
import com.microsoft.portableIdentity.sdk.auth.protocolManagers.OIDCProtocolManager
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import com.microsoft.portableIdentity.sdk.utilities.HttpWrapper
import java.lang.Exception

class Response(private val request: Request, private val signer: Signer = Signer()) {

    private val collectedCredentials: MutableList<Credential> = mutableListOf()

    fun addCredential(credential: Credential) {
        collectedCredentials.add(credential)
    }

    /**
     * 1. Composes ResponseContents from RequestContents and collected credentials.
     * 2. Protects contents with protectors.
     * 3. Sends Response to url.
     *
     * @returns response to Response.
     */
    suspend fun send(): String? {
        var responseBody: String
        when (request.protocolManager) {
            OIDCProtocolManager::class -> {

                if (request.protocolManager.requestContent == null) {
                    return null
                }

                val responseContent = OIDCResponseContent.create(request.protocolManager.requestContent!!, collectedCredentials)
                val token = wrapAsJwsToken(responseContent)
                val serializedToken = token.serialize(JwsFormat.Compact)
                responseBody = "id_token=${serializedToken}"
                if (!responseContent.state.isNullOrBlank()) {
                   responseBody += "&state=${responseContent.state}"
                }
            }
            else -> {
                throw Exception("Protocol Not Supported")
            }
        }
        return HttpWrapper.post(responseBody, request.protocolManager.requestContent!!.responseUri)
    }

    /**
     * Create JwsToken from ResponseContent and sign if signer exists.
     *
     * @param responseContent content to sign.
     *
     * @return JwsToken signed if signer exists.
     */
    private fun wrapAsJwsToken(responseContent: ResponseContent): JwsToken {

        // create JWSToken and sign if signer is not null.
        return if (signer != null) {
            // use signer to sign payload.
            responseContent.addSignerParams(signer!!)
            signer!!.protect(responseContent)
            // TODO(get rid of bangs)
        } else {
            // do not sign token.
            JwsToken(responseContent.stringify(), BaseLogger)
        }
    }

    /**
     * Create JoseToken from ResponseContent and encrypt if encryptor exists.
     *
     * @return JweToken
     */
    private fun wrapAsJweToken(): JoseToken {
        TODO("Encrypting not supported.")
    }
}