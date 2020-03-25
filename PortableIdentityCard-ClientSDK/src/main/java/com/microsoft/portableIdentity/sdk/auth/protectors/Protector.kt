package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

interface Protector {

    fun protect(content: String) : JwsToken
}