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
     * Wrapper over serializer stringify function.
     */
    fun stringify() : String
}