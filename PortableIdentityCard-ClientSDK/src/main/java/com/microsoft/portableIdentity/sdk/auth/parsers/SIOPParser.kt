package com.microsoft.portableIdentity.sdk.auth.parsers

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.models.OIDCRequestObject
import com.microsoft.portableIdentity.sdk.auth.oidc.OidcRequest
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.JoseToken
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer

/**
 * Object that parses a OpenID Connect Self-Issued token.
 */
class SIOPParser(token: JwsToken) : Parser {

    var siopRequest: OIDCRequestObject? = null

    init {
        siopRequest = Serializer.parse(OIDCRequestObject.serializer(), token.content())
        TODO("separate out siop and oidc protocol")
    }

    override fun getCredentialRequests(): CredentialRequests {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}