package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.auth.OidcRequest

const val defaultRegistrationUrl = "beta.discover.did.microsoft.com"
const val defaultResolverUrl = "beta.ion.microsoft.com"

class Agent (keyStore: IKeyStore?,
             registrationUrl: String = defaultRegistrationUrl,
             resolverUrl: String = defaultResolverUrl) {

    /**
     * Creates a master Identifier.
     */
    fun createIdentifier() {}

    /**
     * Creates an OIDC Request.
     */
    fun createOidcRequest(signer: Identifier,
                          signingKeyReference: String,
                          redirectUrl: String,
                          nonce: String?,
                          state: String?) {}

    /**
     * Verify the signature and parse the OIDC Request
     */
    fun parseOidcRequest(request: String) {
        return OidcRequest(request)
    }

    /**
     * Verify the signature and parse the OIDC Response
     */
    fun parseOidcResponse(response: String) {}

}