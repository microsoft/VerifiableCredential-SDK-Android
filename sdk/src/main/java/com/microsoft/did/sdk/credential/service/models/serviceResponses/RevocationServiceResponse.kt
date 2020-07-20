// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.serviceResponses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RevocationServiceResponse(
    @SerializedName("receipt")
    @Expose
    val receipt: HashMap<String, String>
): ServiceResponse