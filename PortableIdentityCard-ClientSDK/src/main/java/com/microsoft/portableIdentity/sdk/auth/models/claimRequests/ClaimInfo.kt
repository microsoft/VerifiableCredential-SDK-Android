/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.claimRequests

import kotlinx.serialization.Serializable

@Serializable
data class ClaimInfo (
    // name of the claim.
    val claim: String,

    // if claim is required
    val required: Boolean? = null,

    // type of object the claim should be.
    val type: String? = null
)