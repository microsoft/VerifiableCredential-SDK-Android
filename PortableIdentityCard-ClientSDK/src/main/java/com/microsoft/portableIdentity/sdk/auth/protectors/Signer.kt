package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import java.security.KeyStore

class Signer(val keyReference: String?): IProtector {
    override fun protect() : JwsToken {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}