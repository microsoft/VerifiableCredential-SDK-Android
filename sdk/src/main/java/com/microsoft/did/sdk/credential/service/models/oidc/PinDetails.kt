// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class PinDetails(
    val length: Int,
    val type: String,
    val salt: String? = null
)