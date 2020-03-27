/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.contracts.display

import kotlinx.serialization.Serializable

/**
 * A user experience data file that describes how information in a Verifiable Credential may be displayed.
 */
@Serializable
data class Display (

    // Must be set to "display"
    val id: String = "display",

    // What locale the display information is in.
    val locale: String,

    // URL pointing to the contract.
    val contract: String,

    // Properties used to render the card
    val card: CardDisplayProperties,

    // Properties used to render the prompt in order to get the card.
    val consent: ConsentDisplayProperties,

    // Mapping of claims in the Verifiable Credential to how to display them.
    val claims: Map<String, ClaimDisplayProperties>
)