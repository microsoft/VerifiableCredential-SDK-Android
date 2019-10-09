package com.microsoft.did.sdk.auth

import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.PercentEncoding

/**
 * Class to represent Open ID Connect Self-Issued Tokens
 * @class
 */
class OidcRequest(
    val sender: Identifier,
    val redirectUrl: String,
    val nonce: String,
    val claimObjects: List<ClaimObject>?,
    val state: String?,
    val claimsRequested: List<String>?
) {

    companion object {
        /**
         * Standard response type for SIOP.
         */
        const val responseType = "id_token"

        /**
         * Standard response mode for SIOP.
         */
        const val responseMode = "form_post"

        /**
         * Standard scope for SIOP.
         */
        const val scope = "openid did_authn"

        suspend fun parseAndVerify(signedRequest: String, crypto: CryptoOperations, resolver: IResolver): OidcRequest {
            if (!Regex("^openid\\:\\/\\/").matches(signedRequest)) {
                throw Error("Must be passed a string beginning in \"openid://\"")
            }
            // do we have a direct or indirect request object?
            val scope = Regex("scope=([^&]+)").find(signedRequest)
            val responseType = Regex("response_type=([^&]+)").find(signedRequest)
            val redirectUrl = Regex("client_id=([^&]+)").find(signedRequest)
            // optionals
            val claims = Regex("claims=([^&]+)").find(signedRequest)
            val registration = Regex("registration=([^&]+)").find(signedRequest)
            var request = Regex("request=([^&]+)").find(signedRequest)
            val indirectRequest = Regex("request_uri=([^&]+)").find(signedRequest)
            // go here for the request
            if (indirectRequest != null) {
                val url = PercentEncoding.decode(indirectRequest.groupValues[1])
            }

            val token = JwsToken(signedRequest)
            // verify the token
            val content = token.content()
            TODO("parseAndVerify not yet implemented.")
        }
    }

    /**
     * Respond to OIDC Request using identifier.
     * @param identifier the identifier used to sign response
     */
    suspend fun respondWith(identifier: Identifier, claimObjects: List<ClaimObject>? = null) {
        TODO("Not implemented")
    }
}