package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Static class that Validates an OIDC Request.
 */
@Singleton
class OidcRequestValidator @Inject constructor(private val jwsValidator: JwsValidator){

    /**
     * Verifies that Oidc request.
     *
     * @param request to be validated.
     * @return true, if valid.
     */
    suspend fun validate(request: OidcRequest): Boolean {
        return request.content.exp != null
                && jwsValidator.verifySignature(request.token)
                && hasTokenExpired(request.content.exp)
                && hasMatchingParams(request.content, request.oidcParameters)
    }

    private fun hasTokenExpired(expiration: Long): Boolean {
        val expirationOffset = getExpirationOffset()
        return expiration > expirationOffset
    }

    private fun getExpirationOffset(): Long {
        val currentTime = Date().time
        val milliseconds = 1000
        val expirationCheckTimeOffsetInMinutes = 5
        return currentTime + milliseconds * 60 * expirationCheckTimeOffsetInMinutes
    }

    private fun hasMatchingParams(requestContents: OidcRequestContent, params: Map<String, List<String>>): Boolean {
        return params["client_id"]?.first() == requestContents.clientId
    }
}