package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocumentPatch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OperationData (
    @SerialName("nextUpdateOtpHash")
    val nextUpdateCommitmentHash: String,
    @SerialName("patches")
    val patches: List<IdentifierDocumentPatch>
) {}