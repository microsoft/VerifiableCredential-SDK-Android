// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import com.microsoft.did.sdk.credential.models.RegexSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val type: String,
    @Serializable(with = RegexSerializer::class)
    val pattern: String
)
