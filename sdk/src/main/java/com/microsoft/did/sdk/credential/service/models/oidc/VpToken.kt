// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VpToken(
    @SerialName("presentation_submission")
    val presentationSubmission: PresentationSubmission = PresentationSubmission(emptyList())
)
