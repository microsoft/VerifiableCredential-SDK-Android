// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.serviceResponses

import kotlinx.serialization.Serializable

/**
 * Error Object get back from Issuance Service if Error occurred.
 */
@Serializable
data class IssuanceServiceError(
    val httpStatusCode: Int? = null,
    val expose: Boolean? = false,
    val code: String = "Issuance Service Error"
)