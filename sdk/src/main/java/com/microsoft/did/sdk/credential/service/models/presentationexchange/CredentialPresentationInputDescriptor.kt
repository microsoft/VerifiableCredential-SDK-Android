// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialPresentationInputDescriptor(
    var id: String,

    @SerialName("schema")
    val schemas: List<Schema>,

    val name: String = "",

    val purpose: String = "",

    @SerialName("issuance")
    var issuanceMetadataList: List<IssuanceMetadata> = emptyList(),

    var constraints: Constraints? = null
)