/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.contracts.display

import kotlinx.serialization.Serializable

/**
 * Properties that are used to render a Portable Identity Card.
 * These properties are not user-specific, but used to render a generic representation of the card.
 */
@Serializable
data class CardDisplayProperties (

    // Title of the Card.
    val title: String,

    // What entity issued the card, "Woodgrove Bank" for example.
    val issuedBy: String,

    // The background color of the card in hex.
    val backgroundColor: String,

    // The color of the text written on card in hex.
    val textColor: String,

    // Logo that should be displayed on the card.
    val logo: LogoDisplayProperties,

    // Description of the card that should be displayed below the card.
    val description: String
)