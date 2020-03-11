package com.microsoft.portableIdentity.sdk.auth.parsers

import com.microsoft.portableIdentity.sdk.auth.ProtocolType
import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

interface IParser {

    /**
     * Tries to serialize contents of the token until successful.
     * @return RequestContent object and ProtocolType.
     */
    fun parse(token: JwsToken) : Pair<RequestContent, ProtocolType>
}