package com.microsoft.portableIdentity.sdk.auth.validators

import android.net.Uri
import com.microsoft.portableIdentity.sdk.utilities.Constants.CLIENT_ID
import com.microsoft.portableIdentity.sdk.utilities.Constants.MILLISECONDS_IN_A_SECOND
import com.microsoft.portableIdentity.sdk.utilities.Constants.SECONDS_IN_A_MINUTE
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ValidatorException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates an OpenID Connect Request.
 */
@Singleton
class OidcPresentationRequestValidator @Inject constructor(private val jwsValidator: JwsValidator,
                                                           private val serializer: Serializer) : PresentationRequestValidator {

    override suspend fun validate(request: PresentationRequest) {
        val token = JwsToken.deserialize(request.serializedToken, serializer)
        if (!jwsValidator.verifySignature(token)) {
            throw ValidatorException("Signature is not Valid.")
        }
        // TODO(check token expiration when implemented in sdk)
        // checkTokenExpiration(request.content.exp)
        checkRequestParameters(request.content, request.uri)
    }

    private fun checkTokenExpiration(expiration: Long) {
        if (getExpirationDeadlineInSeconds() > expiration) {
            throw ValidatorException("Request Token has expired.")
        }
    }

    private fun getExpirationDeadlineInSeconds(expirationCheckTimeOffsetInMinutes: Int = 5): Long {
        val currentTimeInSeconds = Date().time / MILLISECONDS_IN_A_SECOND
        return currentTimeInSeconds + SECONDS_IN_A_MINUTE * expirationCheckTimeOffsetInMinutes
    }

    private fun checkRequestParameters(requestContents: OidcRequestContent, uri: Uri) {
        if (uri.getQueryParameter(CLIENT_ID) != requestContents.clientId) {
            throw ValidatorException("Request content does not match url parameters.")
        }
    }
}