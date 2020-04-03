// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.cards.verifiableCredential

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class VerifiableCredential(
        val raw: String,

        val contents: VerifiableCredentialContent
)