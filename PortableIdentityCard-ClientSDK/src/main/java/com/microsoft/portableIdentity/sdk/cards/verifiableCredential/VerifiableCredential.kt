<<<<<<< HEAD
// Copyright (c) Microsoft Corporation. All rights reserved

=======
>>>>>>> master
package com.microsoft.portableIdentity.sdk.cards.verifiableCredential

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class VerifiableCredential(
<<<<<<< HEAD
        val raw: String,

        val contents: VerifiableCredentialContent
=======

    val raw: String,

    val contents: VerifiableCredentialContent
>>>>>>> master
)