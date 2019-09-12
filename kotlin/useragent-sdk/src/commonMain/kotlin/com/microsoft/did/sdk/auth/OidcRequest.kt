package com.microsoft.did.sdk

import com.microsoft.did.sdk.identifier.Identifier

const val responseType = "id_token"
const val responseMode = "form_post"
const val scope = "openid did_authn"

/**
 * Class to represent Open ID Connect Self-Issued Tokens
 * @class
 */
class OidcRequest(
    sender: Identifier,
    redirectUrl: String,
    nonce: String
) {

    /**
     * Respond to OIDC Request using identifier.
     * @param identifier the identifier used to sign response
     */
    fun respondWith(identifier: Identifier, keyReference: String) {}
}