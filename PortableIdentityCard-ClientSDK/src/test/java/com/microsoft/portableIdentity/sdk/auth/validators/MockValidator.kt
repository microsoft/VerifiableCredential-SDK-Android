package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

class MockValidator: IValidator {

    /**
     * always return true
     */
    override suspend fun verify(token: JwsToken, requester: String): Boolean {
        return true
    }
}