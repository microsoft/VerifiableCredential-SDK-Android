package com.microsoft.portableIdentity.sdk.auth.protocolManagers

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.auth.protectors.Protector
import com.microsoft.portableIdentity.sdk.auth.validators.Validator

interface ProtocolManager {

    val responseUri: String

    /**
     * Gets the Credential Requests.
     */
    fun getCredentialRequests(): CredentialRequests

    /**
     * Validates the request.
     *
     * @param validator used to valid request token.
     */
    suspend fun isRequestValid(validator: Validator): Boolean

    /**
     * Form Response and protect it with Protector.
     *
     * @param protector used to protect response.
     * @param collectedCredentials credentials to be sent in response.
     */
    fun formResponse(protector: Protector, collectedCredentials: List<Credential>): String
}