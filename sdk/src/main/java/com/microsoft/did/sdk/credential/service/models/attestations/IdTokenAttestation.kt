/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

@Serializable
data class IdTokenAttestation(

    val claims: List<ClaimAttestation>,

    val configuration: String,

    val client_id: String,

    val required: Boolean = false,

    val redirect_uri: String = "",

    val scope: String = "openid",

    val encrypted: Boolean = false
)