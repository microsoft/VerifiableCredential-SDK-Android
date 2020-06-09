// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.verifiablePresentation

import com.microsoft.did.sdk.util.Constants.CONTEXT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifiablePresentationDescriptor(
    @SerialName(CONTEXT)
    val context: List<String>,

    val type: List<String>,

    val verifiableCredential: List<String>
)