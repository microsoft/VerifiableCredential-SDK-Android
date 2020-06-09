// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.identifier.models.Identifier
import kotlinx.serialization.Serializable

/**
 * A VerifiableCredentialContainer holds a VerifiableCredential and additional meta-data like history and the display contract
 */
@Entity
@Serializable
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
data class VerifiableCredentialContainer(

    // id of the prime Verifiable Credential
    @PrimaryKey
    val cardId: String,

    // verifiable credential tied to Pairwise Identifier for Issuer.
    @Embedded
    val verifiableCredential: VerifiableCredential,

    @Embedded
    val owner: Identifier,

    val displayContract: DisplayContract
)