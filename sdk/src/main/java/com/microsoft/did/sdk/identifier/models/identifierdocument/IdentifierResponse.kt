// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.models.identifierdocument

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("document")
data class IdentifierResponse(
    @SerialName("didDocument")
    val didDocument: IdentifierDocument,
    @SerialName("@context")
    val context: String = "https://www.w3.org/ns/did-resolution/v1"
) {
    @SerialName("methodMetadata")
    val identifierMetadata: IdentifierMetadata? = null
}