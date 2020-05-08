/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class AttestationClaimModel(
        val selfIssued: Map<String, String>?,

        val idTokens: Map<String, String>?,

        val presentations: Map<String, String>?
)