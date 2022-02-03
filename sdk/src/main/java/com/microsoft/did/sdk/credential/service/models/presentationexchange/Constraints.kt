// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.Serializable

@Serializable
data class Constraints(
    var fields: List<Fields> = emptyList()
)