package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.utilities.Constants.CLIENT_ID
import com.microsoft.portableIdentity.sdk.utilities.Constants.MILLISECONDS_IN_A_SECOND
import com.microsoft.portableIdentity.sdk.utilities.Constants.SECONDS_IN_A_MINUTE
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ValidatorException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates an OpenID Connect Request.
 */
@Singleton
class OidcRequestValidator @Inject constructor(private val jwsValidator: JwsValidator,
                                               private val serializer: Serializer) : Validator {

    override suspend fun validate(request: Request): Result<Boolean> {
        if (request !is PresentationRequest) {
            val exception = ValidatorException("Request is not an OidcRequest")
            return Result.Failure(exception)
        }

        val token = JwsToken.deserialize(request.serializedToken, serializer)
        return when (val validationResult = jwsValidator.verifySignature(token)) {
            is Result.Success -> {
                val isValid = validationResult.payload && hasTokenExpired(request.contents.exp) && hasMatchingParams(request.contents, request.oidcParameters)
                Result.Success(isValid)
            }
            is Result.Failure -> validationResult
        }
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