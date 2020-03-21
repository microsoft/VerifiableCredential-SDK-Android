package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken

interface Protector {

    fun protect(content: String) : JoseToken
}