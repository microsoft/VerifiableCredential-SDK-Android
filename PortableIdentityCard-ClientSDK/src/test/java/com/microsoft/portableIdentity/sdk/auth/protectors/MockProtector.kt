package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken

class MockProtector: IProtector {
    override fun protect(responseContent: ResponseContent): JoseToken {
        TODO("create a mock JoseToken")
    }
}