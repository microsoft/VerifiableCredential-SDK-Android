package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.identifier.Identifier

class Agent (keyStore: IKeyStore?,
             crypto: Subtle?,
             registerUrl: String = Constants.RegisterUrl.value,
             resolverUrl: String = Constants.ResolverUrl.value) {

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
    fun parseOidcRequest(request: String) {}

    /**
     * Verify the signature and parse the OIDC Response
     */
    fun parseOidcResponse(response: String) {}

}