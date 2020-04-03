package com.microsoft.portableIdentity.sdk.identifier.models.document

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class IdentifierDocumentPatch (
    val action: String,
    @SerialName("document")
    val document: IdentifierDocumentPayload
){}