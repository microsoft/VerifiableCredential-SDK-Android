// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.serviceResponses

import kotlinx.serialization.Serializable

@Serializable
data class ContractServiceResponse(
    val token: String
)