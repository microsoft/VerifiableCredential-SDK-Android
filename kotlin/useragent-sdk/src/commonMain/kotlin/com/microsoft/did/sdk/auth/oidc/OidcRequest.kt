package com.microsoft.did.sdk.auth

import com.microsoft.did.sdk.auth.oidc.Registration
import com.microsoft.did.sdk.credentials.ClaimObject
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.PercentEncoding
import com.microsoft.did.sdk.utilities.getHttpClient
import io.ktor.client.request.get
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.parseMap
import kotlin.math.sign

/**
 * Class to represent Open ID Connect Self-Issued Tokens
 * @class
 */
class OidcRequest private constructor(
    val sender: Identifier,
    val redirectUrl: String,
    val nonce: String?,
    val claimObjects: List<ClaimObject>?,
    val state: String?,
    val claimsRequested: List<String>?,
    val scope: String = OidcRequest.scope,
    val responseType: String = OidcRequest.responseType,
    val responseMode: String = OidcRequest.responseMode
) {


    @Serializable
    private data class OidcRequestObject(
        val iss: String?,
        val aud: String?,
        @SerialName("response_type")
        val responseType: String?,
        @SerialName("response_mode")
        val responseMode: String?,
        @SerialName("client_id")
        val clientId: String?,
        @SerialName("redirect_uri")
        val redirectUri: String?,
        val scope: String?,
        val state: String?,
        val nonce: String?,
        @SerialName("max_age")
        val maxAge: Int?,
        val claims: OidcRequestClaim?,
        val registration: Registration?
        // custom extension values

    ) {
        @Serializable
        private data class OidcRequestClaim(
            val userInfo: UserInfo?,
            @SerialName("id_token")
            val idToken: IdToken?
        ) {
            @Serializable
            private data class UserInfo(
                val name: String?,
                @SerialName("given_name")
                val givenName: String?,
                @SerialName("family_name")
                val familyName: String?,
                @SerialName("middle_name")
                val middleName: String?,
                val nickname: String?,
                @SerialName("preferred_username")
                val preferredUsername: String?,
                val profile: String?,
                val picture: String?,
                val website: String?,
                val email: String?,
                @SerialName("email_verified")
                val emailVerified: Boolean?,
                val gender: String?,
                val birthdate: String?,
                val zoneinfo: String?,
                val locale: String?,
                @SerialName("phone_number")
                val phoneNumber: String?,
                @SerialName("phone_number_verified")
                val phoneNumberVerified: Boolean?,
                val address: Address?,
                @SerialName("updated_at")
                val updatedAt: Int?
            ) {
                @Serializable
                private data class Address(
                    val formatted: String?,
                    @SerialName("street_address")
                    val streetAddress: String?,
                    val locality: String?,
                    val region: String?,
                    @SerialName("postal_code")
                    val postalCode: String?,
                    val country: String?
                )
            }
        }
    }

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

        @ImplicitReflectionSerializer
        suspend fun parseAndVerify(signedRequest: String,
                                   crypto: CryptoOperations,
                                   resolver: IResolver): OidcRequest {
            if (!Regex("^openid\\:\\/\\/").matches(signedRequest)) {
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
            val token = JwsToken(request)
            // get the DID associated
            val contents = MinimalJson.serializer.parse(OidcRequestObject.serializer(), token.content());
            if (contents.iss.isNullOrBlank()) {
                throw Error("Could not find the issuer's DID")
            }
            val sender = resolver.resolve(contents.iss, crypto)
            // verify the request
            val keys = sender.document.publicKeys.map {
                it.toPublicKey()
            }
            token.verify(crypto, keys)
            // retrieve the rest of the parameters
            val scope = contents.scope ?: getQueryStringParameter(OAuthRequestParameter.Scope, signedRequest, true)!!
            var responseType = contents.responseType ?: (OAuthRequestParameter.ResponseType, signedRequest, true)!!
            var redirectUrl = contents.clientId ?: contents.redirectUri ?:
                getQueryStringParameter(OAuthRequestParameter.ClientId, signedRequest true)!!
            // optionals
            val state = contents.state ?: getQueryStringParameter(OAuthRequestParameter.State, signedRequest)
            val responseMode = contents.responseMode ?: getQueryStringParameter(OAuthRequestParameter.ResponseMode, signedRequest)
            val nonce = contents.nonce ?: getQueryStringParameter(OAuthRequestParameter.Nonce, signedRequest)
            var claims = contents.claims ?: getQueryStringParameter(OAuthRequestParameter.Claims, signedRequest)
            var registration = if (contents.registration != null) {
                contents.registration
            } else {
                val registrationValue = getQueryStringParameter(OAuthRequestParameter.Registration, signedRequest)
                if (!registrationValue.isNullOrBlank()) {
                    MinimalJson.serializer.parse(Registration.serializer(), registrationValue)
                } else {
                    null
                }
            }

            // form an OidcRequest object
            return OidcRequest(
                sender,
                redirectUrl,
                nonce,
                ,
                state,
                ,
                scope,
                responseType,
                responseMode
            )
        }

        private fun getQueryStringParameter(name: OAuthRequestParameter, url: String, required: Boolean = false): String? {
            val findResults = Regex("${name.value}=([^&]+)").find(url)
            if (findResults != null) {
                return PercentEncoding.decode(findResults.groupValues[1])
            } else if (required) {
                throw Error("Openid requires a \"${name.value}\" parameter")
            }
            return null
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