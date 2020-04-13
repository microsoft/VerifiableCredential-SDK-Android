// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.identifier.models.payload.document

import kotlinx.serialization.Serializable

@Serializable
data class IdentifierDocumentService (
    val id: String,
    val type: String,
    val serviceEndpoint: String
)