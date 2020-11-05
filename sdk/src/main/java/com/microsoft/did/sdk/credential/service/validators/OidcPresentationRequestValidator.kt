package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.util.Constants.MILLISECONDS_IN_A_SECOND
import com.microsoft.did.sdk.util.Constants.SECONDS_IN_A_MINUTE
import com.microsoft.did.sdk.util.controlflow.ExpiredTokenExpirationException
import com.microsoft.did.sdk.util.controlflow.MissingInputInRequestException
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates an OpenID Connect Request.
 */
@Singleton
class OidcPresentationRequestValidator @Inject constructor() : PresentationRequestValidator {

    override suspend fun validate(request: PresentationRequest) {
        checkTokenExpiration(request.content.expirationTime)
        checkForInputInPresentationRequest(request)
    }

    private fun checkTokenExpiration(expiration: Long) {
        if (getExpirationDeadlineInSeconds() > expiration) {
            throw ExpiredTokenExpirationException("Request Token has expired.")
        }
    }

    private fun getExpirationDeadlineInSeconds(expirationCheckTimeOffsetInMinutes: Int = 5): Long {
        val currentTimeInSeconds = Date().time / MILLISECONDS_IN_A_SECOND
        return currentTimeInSeconds - SECONDS_IN_A_MINUTE * expirationCheckTimeOffsetInMinutes
    }

    private fun checkForInputInPresentationRequest(request: PresentationRequest) {
        if (request.getPresentationDefinition().credentialPresentationInputDescriptors.isNullOrEmpty())
            throw MissingInputInRequestException("Input Descriptor is missing in presentation request.")
    }
}