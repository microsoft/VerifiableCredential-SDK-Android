package com.microsoft.portableIdentity.sdk.identifier.models.payload

import com.microsoft.portableIdentity.sdk.identifier.models.payload.document.IdentifierDocumentPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentifierDocumentPatch(
    val action: String,
    @SerialName("document")
    val document: IdentifierDocumentPayload
)