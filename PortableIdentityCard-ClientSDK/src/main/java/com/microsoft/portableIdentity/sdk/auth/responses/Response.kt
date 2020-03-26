package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.did.sdk.credentials.Credential

interface Response {

    /**
     * Add Credential to be put into response.
     *
     * @param credential to be added to response.
     */
    fun addCredential(credential: Credential)

}