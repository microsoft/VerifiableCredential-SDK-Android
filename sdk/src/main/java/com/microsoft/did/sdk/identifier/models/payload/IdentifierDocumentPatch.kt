package com.microsoft.did.sdk.identifier.models.payload

import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class for defining the type of action/patch performed on sidetree and payload for the patch.
 * It could be replacing a document, adding/removing public keys, adding/removing service endpoints.
 * It is always replacing a document in our case presently.
 */
@Serializable
data class IdentifierDocumentPatch(
    val action: String,
    @SerialName("document")
    val document: IdentifierDocumentPayload
)