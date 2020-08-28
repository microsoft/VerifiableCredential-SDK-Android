/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

@Serializable
data class ClaimAttestation(
    val claim: String,

    val required: Boolean = false,

    var type: String = ""
)