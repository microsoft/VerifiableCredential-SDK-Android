// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import androidx.lifecycle.LiveData
import com.microsoft.did.sdk.auth.oidc.OidcRequest
import com.microsoft.did.sdk.auth.oidc.OidcResponse
import com.microsoft.did.sdk.credentials.ClaimObject
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PicManager(private val config: DidSdkConfig) {

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
        config.vcRepository.insert(claim)
    }

    fun getClaims(): LiveData<List<ClaimObject>> {
        return config.vcRepository.getAllClaimObjects()
    }
}