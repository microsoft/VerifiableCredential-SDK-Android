package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.MILLISECONDS_IN_A_SECOND
import com.microsoft.did.sdk.util.Constants.SECONDS_IN_A_MINUTE
import com.microsoft.did.sdk.util.controlflow.ExpiredTokenExpirationException
import com.microsoft.did.sdk.util.controlflow.InvalidResponseModeException
import com.microsoft.did.sdk.util.controlflow.InvalidResponseTypeException
import com.microsoft.did.sdk.util.controlflow.InvalidScopeException
import com.microsoft.did.sdk.util.controlflow.MissingInputInRequestException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates an OpenID Connect Request.
 */
@Singleton
class OidcPresentationRequestValidator @Inject constructor() : PresentationRequestValidator {

    override suspend fun validate(request: PresentationRequest) {
        //TODO: Check for response type once it is changed to id_token
        checkResponseMode(request.content.responseMode)
        checkScope(request.content.scope)
        checkTokenExpiration(request.content.expirationTime)
        checkForInputInPresentationRequest(request)
    }

    private fun checkTokenExpiration(expiration: Long) {
        if (getExpirationDeadlineInSeconds() > expiration) {
            throw ExpiredTokenExpirationException("Request Token has expired.")
        }
    }

    private fun checkResponseType(responseType: String) {
        if (!responseType.equals(Constants.RESPONSE_TYPE, true))
            throw InvalidResponseTypeException("Invalid response type in request.")
    }

    private fun checkResponseMode(responseMode: String) {
        if (!responseMode.equals(Constants.RESPONSE_MODE, true))
            throw InvalidResponseModeException("Invalid response mode in request.")
    }

    private fun checkScope(scope: String) {
        if (!scope.equals(Constants.SCOPE, true))
            throw InvalidScopeException("Invalid scope in request.")
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