package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.did.sdk.credentials.Credential

interface Response {

    /**
     * list of collected credentials to be sent in response.
     */
    val collectedCredentials: List<Credential>

    /**
     * Add Credential to be put into response.
     *
     * @param credential to be added to response.
     */
    fun addCredential(credential: Credential)

}