package com.microsoft.did.sdk.credentials

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ClaimResponse(
    @SerialName("credential-id")
    val id: String,
    val state: String,
    @SerialName("credential")
    val claimObject: ClaimObject
)