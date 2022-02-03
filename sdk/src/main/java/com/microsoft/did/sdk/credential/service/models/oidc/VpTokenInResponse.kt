// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VpTokenInResponse(

    @SerialName("presentation_submission")
    val presentationSubmission: PresentationSubmission
)
