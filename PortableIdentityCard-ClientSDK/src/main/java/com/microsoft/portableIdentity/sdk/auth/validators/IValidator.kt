package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

interface IValidator {

    /**
     * TODO: encrypting not supported.
     */
    fun decrypt(token: JoseToken)

    /**
     * Checks signature on JwsToken.
     */
    fun verify(token: JwsToken): Boolean
}