package com.microsoft.portableIdentity.sdk.auth.models

import com.microsoft.portableIdentity.sdk.auth.protectors.Signer

class MockResponseContent: ResponseContent {
    override fun addSignerParams(signer: Signer) {}

    override fun stringify(): String {
        return ""
    }
}