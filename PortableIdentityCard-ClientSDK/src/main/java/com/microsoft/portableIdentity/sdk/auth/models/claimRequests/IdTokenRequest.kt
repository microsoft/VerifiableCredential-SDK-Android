/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.claimRequests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdTokenRequest (

    val claims: List<ClaimInfo>,

    val configuration: String,

    @SerialName("client_id")
    val clientId: String,

    @SerialName("redirect_uri")
    val redirectUri: String
)