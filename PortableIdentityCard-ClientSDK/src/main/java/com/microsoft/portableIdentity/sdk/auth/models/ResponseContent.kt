package com.microsoft.portableIdentity.sdk.auth.models

import com.microsoft.did.sdk.credentials.Credential
import com.microsoft.portableIdentity.sdk.auth.protectors.Signer

interface ResponseContent {

    /**
     * Add Parameters to Response Content if signing.
     *
     * @param signer that is doing the signing.
     */
    fun addSignerParams(signer: Signer)

    /**
     * Add Credentials to Response Content.
     *
     * @param credentials a list of requested credentials.
     */
    fun addCredentials(credentials: List<Credential>)

    /**
     * Wrapper over serializer stringify function.
     */
    fun stringify() : String
}