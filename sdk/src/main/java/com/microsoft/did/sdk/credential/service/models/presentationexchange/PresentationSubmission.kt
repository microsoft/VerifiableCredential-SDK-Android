// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationSubmission(
    val id: String,

    @SerialName("definition_id")
    val definitionId: String,

    @SerialName("descriptor_map")
    val presentationSubmissionDescriptors: List<PresentationSubmissionDescriptor>
)