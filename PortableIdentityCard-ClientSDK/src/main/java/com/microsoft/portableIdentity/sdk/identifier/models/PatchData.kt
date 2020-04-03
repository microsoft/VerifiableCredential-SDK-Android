package com.microsoft.portableIdentity.sdk.identifier.models

import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPatch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatchData (
    @SerialName("nextUpdateCommitmentHash")
    val nextUpdateCommitmentHash: String,
    @SerialName("patches")
    val patches: List<IdentifierDocumentPatch>
) {}