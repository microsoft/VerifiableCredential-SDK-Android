package com.microsoft.portableIdentity.sdk.auth.models.oidc

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.credentialRequests.RequestInfo
import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.Registration
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Contents of a OpenID Self-Issued Token Request.
 */
@Serializable
data class OIDCRequestContent(

    // what type of object the response should be (should be idtoken).
    @SerialName("response_type")
    val responseType: String? = null,

    // what mode the response should be sent in (should always be form post).
    @SerialName("response_mode")
    val responseMode: String? = null,

    // did of the entity who sent the request.
    @SerialName("client_id")
    override val requester: String,

    // where the SIOP provider should send response to.
    @SerialName("redirect_uri")
    override val responseUri: String = "",

    // should contain "openid did_authN"
    val scope: String? = null,

    // opaque values that should be passed back to the requester.
    val state: String? = null,
    val nonce: String? = null,

    // claims that are being requested.
    val claims: Map<String, Map<String, RequestInfo?>?>? = null,

    // iat and exp that need to be checked to see if token has expired
    val exp: String? = null,
    val iat: String? = null,

    // optional parameters
    val registration: Registration? = null,
    val iss: String? = null,
    val aud: String? = null,
    @SerialName("max_age")
    val maxAge: Int? = null,
    val nbf: String? = null
): RequestContent {

    /**
     * Get OIDC Specific Params.
     */
    fun getOIDCParams(): Map<String, String?> {
        return mapOf(
            "response_type" to responseType,
            "response_mode" to responseMode,
            "client_id" to requester,
            "redirect_url" to responseUri,
            "scope" to scope,
            "state" to state,
            "nonce" to nonce
        )
    }

}