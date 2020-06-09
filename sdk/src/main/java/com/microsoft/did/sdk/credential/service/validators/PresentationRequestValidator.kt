package com.microsoft.did.sdk.credential.service.validators

import com.microsoft.did.sdk.credential.service.PresentationRequest

interface PresentationRequestValidator {

    suspend fun validate(request: PresentationRequest)
}