// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.deprecated.oidc.OidcResponse
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardManager(private val config: DidSdkConfig) {

    fun parseOidcRequest(request: String, callback: (OidcRequest) -> Unit) {
        GlobalScope.launch {
            callback.invoke(parseOidcRequest(request))
        }
    }

    fun parseOidcResponse(request: String, callback: (OidcResponse) -> Unit) {
        GlobalScope.launch {
            callback.invoke(parseOidcResponse(request))
        }
    }

    /**
     * Verify the signature and
     * return OIDC Request object.
     */
    suspend fun parseOidcRequest(request: String): OidcRequest {
        return withContext(Dispatchers.IO) {
            OidcRequest.parseAndVerify(request, config.cryptoOperations, config.logger, config.resolver)
        }
    }

    /**
     * Verify the signature and
     * parse the OIDC Response object.
     */
    suspend fun parseOidcResponse(
        response: String,
        clockSkewInMinutes: Int = 5,
        issuedWithinLastMinutes: Int? = null,
        contentType: ContentType = ContentType.Application.FormUrlEncoded
    ): OidcResponse {
        return withContext(Dispatchers.IO) {
            OidcResponse.parseAndVerify(
                response, clockSkewInMinutes, issuedWithinLastMinutes,
                config.cryptoOperations, config.logger, config.resolver, contentType
            )
        }
    }

    suspend fun saveClaim(claim: ClaimObject) {
        config.repository.saveClaim(claim)
    }

    suspend fun getClaims(): List<ClaimObject> {
        return config.repository.getClaims()
    }
}