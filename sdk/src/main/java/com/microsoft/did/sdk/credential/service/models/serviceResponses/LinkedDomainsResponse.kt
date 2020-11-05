/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.serviceResponses

import com.microsoft.did.sdk.util.Constants.CONTEXT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkedDomainsResponse(
    @SerialName(CONTEXT)
    val context: String,
    @SerialName("linked_dids")
    val linkedDids: List<String>
)