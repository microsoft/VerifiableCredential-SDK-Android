package com.microsoft.portableIdentity.sdk.auth.validators

import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest

interface PresentationRequestValidator {

    suspend fun validate(request: PresentationRequest)
}