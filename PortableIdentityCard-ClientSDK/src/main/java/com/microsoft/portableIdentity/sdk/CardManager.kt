// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.utilities.HttpWrapper
import io.ktor.http.Url
import io.ktor.util.toMap

class CardManager(private val config: DidSdkConfig) {

    /**
     * Create a Request Object from a uri.
     */
    suspend fun getRequest(uri: String): Request {
        val url = Url(uri)
        if (url.protocol.name != "openid") {
            throw Exception("request format not supported")
        }

        val requestParameters = url.parameters.toMap()
        val serializedToken = requestParameters["request"]?.first()
        if (serializedToken != null) {
            return OidcRequest(requestParameters, serializedToken)
        }

        val requestUri = requestParameters["request_uri"]?.first()
        val requestToken = HttpWrapper.get(requestUri!!)
        return OidcRequest(requestParameters, requestToken)
    }

    /**
     * Validate an OpenID Connect Request.
     */
    suspend fun validate(request: OidcRequest) {
        OidcRequestValidator.validate(request)
    }

    /**
     * Send a Response
     */
    suspend fun send(response: OidcResponse) {
        TODO("use retrofit for API calls")
    }

    /**
     *
     */
    suspend fun saveCard(claim: ClaimObject) {
        config.repository.saveClaim(claim)
    }

    suspend fun getCards(): List<ClaimObject> {
        return config.repository.getClaims()
    }
}