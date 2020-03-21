/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.protocolManagers.OIDCProtocolManager
import com.microsoft.portableIdentity.sdk.auth.protocolManagers.ProtocolManager
import com.microsoft.portableIdentity.sdk.auth.validators.Validator
import com.microsoft.portableIdentity.sdk.auth.validators.JoseValidator

/**
 * Class that represents a generic Request.
 *
 * @param rawRequest to be parsed.
 * @param validator optional parameter used to validate request.
 */
class Request(private val rawRequest: String, private val validator: Validator = JoseValidator()) {

    /**
     * manages protocol specific logic.
     */
    val protocolManager: ProtocolManager

    init {
        // only support OIDC protocol for now.
        // which protocol manager to create will be decided in this constructor in the future.
        protocolManager = OIDCProtocolManager(rawRequest)
    }


    /**
     * Validate this request using the Validator.
     * This includes:
     * 1. verifying the signature of the JWSToken
     * 2. check the claims of content protocol.
     *
     * @return true if successfully validate.
     */
    suspend fun isValid(): Boolean {
        return protocolManager.isRequestValid(validator)
    }

    /**
     * Get Credential Requests if there are any in Request.
     *
     * @return credentials requests if exist, null if no credentials requested.
     */
    fun getCredentialRequests(): CredentialRequests? {
        return protocolManager.getCredentialRequests()
    }
}