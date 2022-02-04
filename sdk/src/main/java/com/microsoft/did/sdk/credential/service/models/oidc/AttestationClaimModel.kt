/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class AttestationClaimModel(
    val selfIssued: Map<String, String> = emptyMap(),

    val idTokens: Map<String, String> = emptyMap(),

    val accessTokens: Map<String, String> = emptyMap(),

    val presentations: Map<String, String> = emptyMap()
)