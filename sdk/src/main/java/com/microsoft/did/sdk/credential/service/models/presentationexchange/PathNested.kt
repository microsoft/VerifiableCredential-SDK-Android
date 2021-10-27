// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PathNested(
    @SerialName("id")
    val idFromInputDescriptor: String = "",

    val format: String = "",

    val path: String = "",

    @SerialName("path_nested")
    var pathNested: PathNested? = null
)
