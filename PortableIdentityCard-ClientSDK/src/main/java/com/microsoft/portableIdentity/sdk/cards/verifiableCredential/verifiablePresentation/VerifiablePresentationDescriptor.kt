// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.cards.verifiableCredential.verifiablePresentation

import com.microsoft.portableIdentity.sdk.utilities.Constants.CONTEXT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifiablePresentationDescriptor(
    @SerialName(CONTEXT)
    val context: List<String>,

    val type: List<String>,

    val verifiableCredential: List<String>
)