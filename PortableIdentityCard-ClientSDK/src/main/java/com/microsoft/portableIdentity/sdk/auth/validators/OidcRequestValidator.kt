package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.utilities.Constants.CLIENT_ID
import com.microsoft.portableIdentity.sdk.utilities.Constants.MILLISECONDS_IN_A_SECOND
import com.microsoft.portableIdentity.sdk.utilities.Constants.SECONDS_IN_A_MINUTE
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Static class that Validates an OIDC Request.
 */
@Singleton
class OidcRequestValidator @Inject constructor(private val jwsValidator: JwsValidator) {

    /**
     * Verifies that Oidc request.
     *
     * @param request to be validated.
     * @return true, if valid.
     */
    suspend fun validate(request: OidcRequest): Result<Boolean, Exception> {
        return when (val verified = jwsValidator.verifySignature(request.token)) {
            is Result.Success -> Result.Success(
                verified.payload && hasTokenExpired(request.content.exp) && hasMatchingParams(
                    request.content,
                    request.oidcParameters
                )
            )
            is Result.Failure -> Result.Failure(verified.payload)
        }
        //return verifiedSignature && hasTokenExpired(request.content.exp) && hasMatchingParams(request.content, request.oidcParameters)
    }

    private fun hasTokenExpired(expiration: Long): Boolean {
        return expiration > getExpirationDeadlineInSeconds()
    }

    private fun getExpirationDeadlineInSeconds(expirationCheckTimeOffsetInMinutes: Int = 5): Long {
        val currentTimeInSeconds = Date().time / MILLISECONDS_IN_A_SECOND
        return currentTimeInSeconds + SECONDS_IN_A_MINUTE * expirationCheckTimeOffsetInMinutes
    }

    private fun hasMatchingParams(requestContents: OidcRequestContent, params: Map<String, List<String>>): Boolean {
        return params[CLIENT_ID]?.first() == requestContents.clientId
    }
}