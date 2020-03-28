/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.attestations

import com.microsoft.portableIdentity.sdk.auth.models.oidc.CLIENT_ID
import com.microsoft.portableIdentity.sdk.auth.models.oidc.REDIRECT_URL
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdTokenAttestation (

    val claims: List<ClaimAttestation>,

    val configuration: String,

    val required: Boolean = false,

    @SerialName(CLIENT_ID)
    val clientId: String,

    @SerialName(REDIRECT_URL)
    val redirectUri: String
)