package com.microsoft.portableIdentity.sdk.auth.models

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey

interface RequestContent {

    /**
     * Get credential requests from content.
     */
    fun getCredentialRequests(): CredentialRequests

    /**
     * Check claim requirements
     */
    fun isValid(): Boolean

    /**
     * Get List of all public keys that can be used to verify Request.
     */
    fun getPublicKeys(): List<PublicKey>
}