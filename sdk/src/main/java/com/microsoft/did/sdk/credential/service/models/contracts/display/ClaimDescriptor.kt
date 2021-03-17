/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.contracts.display

import kotlinx.serialization.Serializable

/**
 * Properties to render a user-specific claim.
 */
@Serializable
data class ClaimDescriptor(

    // What data type the claim is (ex. "Date")
    val type: String,

    // A label used to describe the claim (ex. "Birthday").
    val label: String
)