// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.repository.VerifiableCredentialRepository
import com.microsoft.portableIdentity.sdk.utilities.HttpWrapper
import io.ktor.http.Url
import io.ktor.util.toMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardManager @Inject constructor(
    private val vcRepository: VerifiableCredentialRepository
) {

    suspend fun getRequest(uri: String): OidcRequest {
        val url: Url
        try {
            url = Url(uri)
        } catch (exception: Exception) {
           throw Exception("uri parameter, $uri, is not a properly formed URL.")
        }
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
    suspend fun saveClaim(claim: ClaimObject) {
        vcRepository.insert(claim)
    }

    fun getClaims(): LiveData<List<ClaimObject>> {
        return vcRepository.getAllClaimObjects()
    }
}