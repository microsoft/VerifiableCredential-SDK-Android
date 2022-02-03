// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    val type: String,
    val pattern: String
)
