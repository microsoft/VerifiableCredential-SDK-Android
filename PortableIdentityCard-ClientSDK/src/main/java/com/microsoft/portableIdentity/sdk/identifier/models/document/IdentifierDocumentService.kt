package com.microsoft.portableIdentity.sdk.identifier.models.document

import kotlinx.serialization.Serializable

@Serializable
data class IdentifierDocumentService (
    val id: String,
    val type: String,
    val serviceEndpoint: String
)