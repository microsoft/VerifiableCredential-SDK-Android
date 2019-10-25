package com.microsoft.did.sdk.auth.oidc

import com.microsoft.did.sdk.OidcResponse
import com.microsoft.did.sdk.auth.OAuthRequestParameter
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.getHttpClient
import io.ktor.client.request.get
import kotlinx.serialization.*

/**
 * Class to represent Open ID Connect Self-Issued Tokens
 * @class
 */
class OidcRequest constructor(
    val sender: Identifier,
    val crypto: CryptoOperations,
    val scope: String = OidcRequest.defaultScope,
    val redirectUrl: String,
    val nonce: String,
    val state: String? = null,
    val responseType: String = OidcRequest.defaultResponseType,
    val responseMode: String = OidcRequest.defaultResponseMode,
    val registration: Registration? = null,
    val claimsRequested: RequestClaimParameter? = null,
    val claimsOffered: ClaimObject? = null
) {
    @Serializable
    private data class OidcRequestObject(
        val iss: String? = null,
        val aud: String? = null,
        @SerialName("response_type")
        val responseType: String? = null,
        @SerialName("response_mode")
        val responseMode: String? = null,
        @SerialName("client_id")
        val clientId: String? = null,
        @SerialName("redirect_uri")
        val redirectUri: String? = null,
        val scope: String? = null,
        val state: String? = null,
        val nonce: String? = null,
        @SerialName("max_age")
        val maxAge: Int? = null,
        val claims: RequestClaimParameter? = null,
        val registration: Registration? = null,
        // custom parameters
        @SerialName("offer")
        val claimsOffered: ClaimObject? = null
    )

    companion object {
        /**
         * Standard response type for SIOP.
         */
        const val defaultResponseType = "id_token"

        /**
         * Standard response mode for SIOP.
         */
        const val defaultResponseMode = "form_post"

        /**
         * Standard scope for SIOP.
         */
        const val defaultScope = "openid did_authn"

        @ImplicitReflectionSerializer
        suspend fun parseAndVerify(signedRequest: String,
                                   crypto: CryptoOperations,
                                   resolver: IResolver): OidcRequest {
            if (!signedRequest.startsWith("openid://")) {
                throw Error("Must be passed a string beginning in \"openid://\"")
            }

            // Verify and parse the request object
            var request = getQueryStringParameter(OAuthRequestParameter.Request, signedRequest)
            // check for a request object
            val indirectRequestUrl = getQueryStringParameter(OAuthRequestParameter.RequestUri, signedRequest)
            if (indirectRequestUrl != null) {
                val client = getHttpClient()
                request = client.get<String>(indirectRequestUrl);
            }

            // verify the signature and whatnot from the request object if we have one
            if (request == null) {
                throw Error("Request contains no signed material")
            }
            val token = JwsToken.deserialize(request)
            // get the DID associated
            val contents = MinimalJson.serializer.parse(OidcRequestObject.serializer(), token.content())
            if (contents.iss.isNullOrBlank()) {
                throw Error("Could not find the issuer's DID")
            }

            val sender = resolver.resolve(contents.iss, crypto)
            DidKeyResolver.verifyJws(token, crypto, sender)

            // retrieve the rest of the parameters
            val scope = contents.scope ?: getQueryStringParameter(OAuthRequestParameter.Scope, signedRequest, true)!!
            val responseType = contents.responseType ?: getQueryStringParameter(
                OAuthRequestParameter.ResponseType,
                signedRequest,
                true
            )!!
            val redirectUrl = contents.clientId ?: contents.redirectUri ?: getQueryStringParameter(
                OAuthRequestParameter.ClientId,
                signedRequest,
                true
            )!!
            // optionals
            val state = contents.state ?: getQueryStringParameter(OAuthRequestParameter.State, signedRequest)
            val responseMode =
                contents.responseMode ?: getQueryStringParameter(OAuthRequestParameter.ResponseMode, signedRequest) ?:
                        OidcRequest.defaultResponseMode
            val nonce = contents.nonce ?: getQueryStringParameter(OAuthRequestParameter.Nonce, signedRequest) ?:
                    throw Error("No nonce was included in this OIDC request.")
            val claims = contents.claims ?: getQueryStringJsonParameter(OAuthRequestParameter.Claims, signedRequest, RequestClaimParameter.serializer())
            val registration = contents.registration ?: getQueryStringJsonParameter(OAuthRequestParameter.Registration,
                signedRequest, Registration.serializer())

            val offers = contents.claimsOffered ?: getQueryStringJsonParameter(OAuthRequestParameter.Offer, signedRequest, ClaimObject.serializer())
            // form an OidcRequest object
            return OidcRequest(
                sender,
                crypto,
                scope,
                redirectUrl,
                nonce,
                state,
                responseType,
                responseMode,
                registration,
                claims,
                offers
            )
        }

        private fun <T>getQueryStringJsonParameter(name: OAuthRequestParameter, url: String, serializer: DeserializationStrategy<T>): T? {
            val data = getQueryStringParameter(name, url)
            return if (!data.isNullOrBlank()) {
                MinimalJson.serializer.parse(serializer, data)
            } else {
                null
            }
        }

    }
    /**
     * Respond to OIDC Request using identifier.
     * @param identifier the identifier used to sign response
     */
    @ImplicitReflectionSerializer
    suspend fun respondWith(identifier: Identifier, claimObjects: List<ClaimObject>? = null): ClaimObject? {
        val oidcResponse = OidcResponse.create(this, identifier)
        claimObjects?.forEach {
            oidcResponse.addClaim(it)
        }
        return oidcResponse.signAndSend(15)
    }
}