package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.models.oidc.CLIENT_ID
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

const val MILLISECONDS = 1000
const val MINUTES_TO_SECONDS_CONVERTER = 60
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
        return JwsValidator.verifySignature(request.token) && hasTokenExpired(request.content.exp) && hasMatchingParams(request.content, request.oidcParameters)
    }

    private fun hasTokenExpired(expiration: Long): Boolean {
        return expiration > getExpirationDeadlineInSeconds()
    }

    private fun getExpirationDeadlineInSeconds(expirationCheckTimeOffsetInMinutes: Int = 5): Long {
        val currentTimeInSeconds = Date().time / MILLISECONDS
        return currentTimeInSeconds + MINUTES_TO_SECONDS_CONVERTER * expirationCheckTimeOffsetInMinutes
    }

    private fun hasMatchingParams(requestContents: OidcRequestContent, params: Map<String, List<String>>): Boolean {
        return params[CLIENT_ID]?.first() == requestContents.clientId
    }
}