// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Claims(
    @SerialName("vp_token")
    val vpToken: VpToken,
)