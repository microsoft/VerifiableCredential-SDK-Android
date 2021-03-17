/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.contracts.display

import kotlinx.serialization.Serializable

/**
 * Properties to be used in the user consent prompt
 */
@Serializable
data class ConsentDescriptor(
    // Title that will be display on prompt page.
    val title: String = "",

    // Instructions for what the user will have to do to get Card.
    val instructions: String
)