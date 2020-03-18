package com.microsoft.portableIdentity.sdk.auth.models

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey

interface RequestContent {

    /**
     * the Uri to send the response to.
     */
    val responseUri: String

    /**
     * The identifier who send the request.
     */
    val requester: String

    /**
     * Get credential requests from content.
     */
    fun getCredentialRequests(): CredentialRequests

    /**
     * Check claim requirements
     */
    fun isValid(): Boolean
}