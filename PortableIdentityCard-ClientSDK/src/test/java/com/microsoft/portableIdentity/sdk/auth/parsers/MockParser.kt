package com.microsoft.portableIdentity.sdk.auth.parsers

import com.microsoft.portableIdentity.sdk.auth.ProtocolType
import com.microsoft.portableIdentity.sdk.auth.models.MockRequestContent
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken

class MockParser: IParser {
    override fun parse(token: JwsToken): Pair<RequestContent, ProtocolType> {
        return Pair(MockRequestContent("", ""), ProtocolType.SIOP)
    }
}