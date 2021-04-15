/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.contracts.display

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * A user experience data file that describes how information in a Verifiable Credential may be displayed.
 */
@Entity
@Serializable
data class DisplayContract(

    // Must be set to "display"
    val id: String = "display",

    // What locale the display information is in.
    val locale: String = "",

    // URL pointing to the contract.
    val contract: String = "",

    // Properties used to render the card
    val card: CardDescriptor,

    // Properties used to render the prompt in order to get the card.
    val consent: ConsentDescriptor,

    // Mapping of claims in the Verifiable Credential to how to display them.
    val claims: Map<String, ClaimDescriptor>
)