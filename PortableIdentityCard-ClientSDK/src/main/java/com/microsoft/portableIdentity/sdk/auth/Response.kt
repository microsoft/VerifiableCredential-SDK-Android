/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OIDCResponseContent
import com.microsoft.portableIdentity.sdk.auth.protectors.Protector
import com.microsoft.portableIdentity.sdk.auth.protectors.Signer
import com.microsoft.portableIdentity.sdk.auth.protocolManagers.OIDCProtocolManager
import com.microsoft.portableIdentity.sdk.auth.protocolManagers.ProtocolManager
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsFormat
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import com.microsoft.portableIdentity.sdk.utilities.HttpWrapper
import java.lang.Exception

/**
 * Generic Response formed from a Request.
 *
 * @param request that response is responding to.
 * @param protector optional parameter used to protect the response.
 */
class Response(request: Request, private val protector: Protector = Signer()) {

    /**
     * Manages all of the protocol specific logic.
     */
    private val protocolManager: ProtocolManager = request.protocolManager

    /**
     * list of collected credentials to be sent in response.
     */
    private val collectedCredentials: MutableList<Credential> = mutableListOf()

    /**
     * Add Credential to be put into response.
     *
     * @param credential to be added to response.
     */
    fun addCredential(credential: Credential) {
        collectedCredentials.add(credential)
    }

    /**
     * Forms response and sends to response url
     *
     * @returns http response.
     */
    suspend fun send(): String? {
        val responseBody = protocolManager.formResponse(protector, collectedCredentials)
        return HttpWrapper.post(responseBody, protocolManager.responseUri)
    }
}