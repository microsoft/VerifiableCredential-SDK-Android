// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.issuancecallback

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class IssuanceCompletionResponse(
    var code: IssuanceCompletionCode,
    val state: String,
    var details: IssuanceCompletionErrorDetails? = null
) {

    @Serializable
    enum class IssuanceCompletionCode {
        @SerialName("issuance_successful") ISSUANCE_SUCCESSFUL,
        @SerialName("issuance_failed") ISSUANCE_FAILED
    }

    @Serializable
    enum class IssuanceCompletionErrorDetails {
        @SerialName("user_canceled") USER_CANCELED,
        @SerialName("fetch_contract_error") FETCH_CONTRACT_ERROR,
        @SerialName("issuance_service_error") ISSUANCE_SERVICE_ERROR,
        @SerialName("unspecified_error") UNSPECIFIED_ERROR
    }
}
