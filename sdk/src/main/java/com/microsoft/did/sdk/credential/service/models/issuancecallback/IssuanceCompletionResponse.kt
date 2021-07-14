// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.issuancecallback

class IssuanceCompletionResponse(
    var code: IssuanceCompletionCode,
    val state: String,
    var details: IssuanceCompletionErrorDetails? = null
) {
    enum class IssuanceCompletionCode {
        ISSUANCE_SUCCESSFUL,
        ISSUANCE_FAILED
    }

    enum class IssuanceCompletionErrorDetails {
        USER_CANCELED,
        FETCH_CONTRACT_ERROR,
        LINKED_DOMAIN_ERROR,
        SERVICE_ERROR,
        UNSPECIFIED_ERROR
    }
}
