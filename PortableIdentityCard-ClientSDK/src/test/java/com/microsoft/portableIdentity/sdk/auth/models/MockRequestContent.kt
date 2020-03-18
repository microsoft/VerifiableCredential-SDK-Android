package com.microsoft.portableIdentity.sdk.auth.models

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests

class MockRequestContent(override val responseUri: String,
                         override val requester: String): RequestContent {

    override fun getCredentialRequests(): CredentialRequests {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isValid(): Boolean {
        return true
    }
}