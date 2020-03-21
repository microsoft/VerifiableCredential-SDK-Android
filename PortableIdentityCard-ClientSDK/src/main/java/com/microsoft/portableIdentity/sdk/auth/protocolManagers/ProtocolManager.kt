package com.microsoft.portableIdentity.sdk.auth.protocolManagers

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.auth.validators.Validator

interface ProtocolManager {

    val requestContent: RequestContent?

    /**
     * Gets the Credential Requests from requestContent.
     */
    fun getCredentialRequests(): CredentialRequests

    /**
     * Validates the request.
     */
    suspend fun isValid(validator: Validator): Boolean
}