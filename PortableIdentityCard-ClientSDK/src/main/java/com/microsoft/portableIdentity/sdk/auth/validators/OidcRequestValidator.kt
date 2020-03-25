package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import java.util.*

object OidcRequestValidator {

    suspend fun verifyRequest(request: OidcRequest): Boolean {
        val jwsToken = request.getJwsToken()
        val contents = request.getContents()
        return jwsToken != null && contents?.exp != null
                && JwsValidator.verifySignature(jwsToken)
                && hasTokenExpired(contents.exp)
                && hasMatchingParams(contents, request.requestParameters)
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