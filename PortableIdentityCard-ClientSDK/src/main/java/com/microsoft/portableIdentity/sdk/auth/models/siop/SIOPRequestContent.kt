package com.microsoft.portableIdentity.sdk.auth.models.siop

import com.microsoft.portableIdentity.sdk.auth.credentialRequests.CredentialRequests
import com.microsoft.portableIdentity.sdk.auth.credentialRequests.InputClaim
import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.Registration
import com.microsoft.portableIdentity.sdk.auth.models.RequestContent
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Contents of a OpenID Self-Issued Token Request.
 */
@Serializable
data class SIOPRequestContent(

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
    val claims: Map<String, Map<String, @ContextualSerialization InputClaim?>?>? = null,

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
    override fun getCredentialRequests(): CredentialRequests {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Checks the expiration.
     * TODO(check to see if all parameters are there?
     */
    override fun isValid(): Boolean {
        val currentTime = Date().time
        // check exp
        val milliseconds = 1000
        // set a leeway of 5 minutes.
        val expirationCheckTimeOffsetInMinutes = 5
        val expirationCheck = currentTime + milliseconds * 60 * expirationCheckTimeOffsetInMinutes
        if (exp is String) {
            try {
                val expLong = exp.toLong()
                if (expLong <= expirationCheck) {
                    BaseLogger.log("Token is not expired")
                    return true
                }
                BaseLogger.log("Token is expired.")
                return false
            } catch (exception: NumberFormatException) {
                BaseLogger.error("exp claim is not a number")
                return false
            }
        }
        BaseLogger.log("exp is not present in SIOP Request.")
        return false
    }
}