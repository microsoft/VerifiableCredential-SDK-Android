// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialPresentationInputDescriptors(
    var id: String = "",

    @SerialName("schema")
    val credentialSchema: CredentialSchema = CredentialSchema()
) {
    @SerialName("issuance")
    var credentialIssuanceMetadataList: List<CredentialIssuanceMetadata> = emptyList()
}