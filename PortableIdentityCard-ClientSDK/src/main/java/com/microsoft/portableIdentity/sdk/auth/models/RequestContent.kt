package com.microsoft.portableIdentity.sdk.auth.models

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests

interface RequestContent {

    /**
     * Get credential requests from content.
     */
    fun getCredentialRequests(): CredentialRequests

    /**
     * Check claim requirements
     */
    fun isValid(): Boolean
}