// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.Serializable

@Serializable
data class Fields(
    val path: List<String>,
    var purpose: String = ""
) {
    var filter: Filter? = null
}
