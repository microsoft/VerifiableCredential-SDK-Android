package com.microsoft.portableIdentity.sdk.cards.deprecated

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