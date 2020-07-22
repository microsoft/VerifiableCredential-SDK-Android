/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

@Serializable
data class ClaimAttestation(
    // name of the claim.
    val claim: String,

    // if claim is required
    val required: Boolean = false,

    // type of object the claim should be.
    val type: String? = ""
)