package com.microsoft.portableIdentity.sdk.auth.models.oidc

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.RequestInfo
import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.Registration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contents of a OpenID Self-Issued Token Request.
 */
@Serializable
data class OidcRequestContent(

    // what type of object the response should be (should be idtoken).
    @SerialName("response_type")
    val responseType: String? = null,

    // what mode the response should be sent in (should always be form post).
    @SerialName("response_mode")
    val responseMode: String? = null,

    // did of the entity who sent the request.
    @SerialName("client_id")
    val clientId: String,

    // where the SIOP provider should send response to.
    @SerialName("redirect_uri")
    val redirectUrl: String = "",

    // should contain "openid did_authN"
    val scope: String? = null,

    // opaque values that should be passed back to the requester.
    val state: String? = null,
    val nonce: String? = null,

    // claims that are being requested.
    val claims: Map<String, Map<String, RequestInfo?>?>? = null,

    // iat and exp that need to be checked to see if token has expired
    val exp: Long? = null,
    val iat: Long? = null,

    // optional parameters
    val registration: Registration? = null,
    val iss: String? = null,
    val aud: String? = null,
    @SerialName("max_age")
    val maxAge: Int? = null,
    val nbf: String? = null
)