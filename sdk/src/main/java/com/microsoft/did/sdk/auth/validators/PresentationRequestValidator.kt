package com.microsoft.did.sdk.auth.validators

import com.microsoft.did.sdk.auth.requests.PresentationRequest

interface PresentationRequestValidator {

    suspend fun validate(request: PresentationRequest)
}