package com.microsoft.portableIdentity.sdk.identifier.models.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatchData (
    @SerialName("nextUpdateCommitmentHash")
    val nextUpdateCommitmentHash: String,
    @SerialName("patches")
    val patches: List<IdentifierDocumentPatch>
)