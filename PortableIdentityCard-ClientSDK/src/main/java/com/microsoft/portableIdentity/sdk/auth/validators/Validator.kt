package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

interface Validator {

    /**
     * Checks signature on JwsToken.
     */
    suspend fun verify(token: JwsToken, requester: String): Boolean
}