/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenAttestation(

    val claims: List<ClaimAttestation>,

    val configuration: String,

    val resourceId: String,

    val required: Boolean = false,

    val redirectUri: String = "",

    val scope: String = "openid",

    val encrypted: Boolean = false
)