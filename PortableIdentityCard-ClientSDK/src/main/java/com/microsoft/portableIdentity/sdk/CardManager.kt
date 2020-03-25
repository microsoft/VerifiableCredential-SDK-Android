// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.validators.OidcRequestValidator
import com.microsoft.portableIdentity.sdk.credentials.deprecated.ClaimObject
import com.microsoft.portableIdentity.sdk.utilities.HttpWrapper

class CardManager(private val config: DidSdkConfig) {

    /**
     * Validate a Request.
     */
    suspend fun validate(request: OidcRequest) {
        OidcRequestValidator.verifyRequest(request)
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