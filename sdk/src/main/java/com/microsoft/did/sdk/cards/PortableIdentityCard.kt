/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.cards

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import com.microsoft.did.sdk.auth.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.did.sdk.identifier.Identifier
import kotlinx.serialization.Serializable

/**
 * Data model to describe a Portable Identity Card.
 */
@Entity
@Serializable
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
data class PortableIdentityCard(

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