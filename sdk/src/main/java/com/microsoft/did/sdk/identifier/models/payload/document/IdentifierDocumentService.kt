// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.models.payload.document

import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocLinkedDomainsServiceEndpointSerializer
import kotlinx.serialization.Serializable

/**
 * Data class for defining service endpoint inside document payload sent to Sidetree operation
 */
@Serializable
data class IdentifierDocumentService(
    val id: String,
    val type: String,
    @Serializable(with = IdentifierDocLinkedDomainsServiceEndpointSerializer::class)
    val serviceEndpoint: List<String>
)