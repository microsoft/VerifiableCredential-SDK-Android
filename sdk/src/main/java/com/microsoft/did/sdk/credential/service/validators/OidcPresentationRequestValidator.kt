package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.oidc.PinDetails
import com.microsoft.did.sdk.credential.service.models.oidc.Registration
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.MILLISECONDS_IN_A_SECOND
import com.microsoft.did.sdk.util.Constants.SECONDS_IN_A_MINUTE
import com.microsoft.did.sdk.util.controlflow.InvalidPinDetailsException
import com.microsoft.did.sdk.util.controlflow.InvalidResponseModeException
import com.microsoft.did.sdk.util.controlflow.InvalidResponseTypeException
import com.microsoft.did.sdk.util.controlflow.InvalidScopeException
import com.microsoft.did.sdk.util.controlflow.MissingInputInRequestException
import com.microsoft.did.sdk.util.controlflow.SubjectIdentifierTypeNotSupported
import com.microsoft.did.sdk.util.controlflow.VpFormatNotSupported
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates an OpenID Connect Request.
 */
@Singleton
class OidcPresentationRequestValidator @Inject constructor(private val jwtValidator: JwtValidator) : PresentationRequestValidator {

    override suspend fun validate(request: PresentationRequest) {
        checkResponseMode(request.content.responseMode)
        checkResponseType(request.content.responseType)
        checkScope(request.content.scope)
        checkForInputInPresentationRequest(request)
        checkRegistrationParameters(request.content.registration)
        validatePinDetails(request.content.pinDetails)
    }

    private fun checkRegistrationParameters(registration: Registration) {
        if (!registration.subjectSyntaxTypesSupported.contains(Constants.SUBJECT_IDENTIFIER_TYPE_DID_ION))
            throw SubjectIdentifierTypeNotSupported("The subject identifier type in registration of request is not supported")
        if (registration.vpFormats != null && !registration.vpFormats.jwtVp.contains(Constants.ALGORITHM_SUPPORTED_IN_VP))
            throw VpFormatNotSupported("VP format algorithm in registration of request is not supported")
        if (registration.vpFormats != null && !registration.vpFormats.jwtVc.contains(Constants.ALGORITHM_SUPPORTED_IN_VC))
            throw VpFormatNotSupported("VC format algorithm in registration of request is not supported")
    }

    private fun checkResponseType(responseType: String) {
        if (!responseType.equals(Constants.RESPONSE_TYPE, true))
            throw InvalidResponseTypeException("Invalid response type in presentation request.")
    }

    private fun checkResponseMode(responseMode: String) {
        if (!responseMode.equals(Constants.RESPONSE_MODE, true))
            throw InvalidResponseModeException("Invalid response mode in presentation request.")
    }

    private fun checkScope(scope: String) {
        if (!scope.equals(Constants.SCOPE, true))
            throw InvalidScopeException("Invalid scope in presentation request.")
    }

    private fun getExpirationDeadlineInSeconds(expirationCheckTimeOffsetInMinutes: Int = 5): Long {
        val currentTimeInSeconds = Date().time / MILLISECONDS_IN_A_SECOND
        return currentTimeInSeconds - SECONDS_IN_A_MINUTE * expirationCheckTimeOffsetInMinutes
    }

    private fun checkForInputInPresentationRequest(request: PresentationRequest) {
        if (request.getPresentationDefinition().credentialPresentationInputDescriptors.isNullOrEmpty())
            throw MissingInputInRequestException("Input Descriptor is missing in presentation request.")
    }

    private fun validatePinDetails(pinDetails: PinDetails?) {
        pinDetails ?: return
        val length = pinDetails.length
        val type = pinDetails.type
        if (length < 1) throw InvalidPinDetailsException("PIN length is invalid in request.")
        if (!(type == "numeric" || type == "alphanumeric")) throw InvalidPinDetailsException("PIN type is invalid in request.")
    }
}