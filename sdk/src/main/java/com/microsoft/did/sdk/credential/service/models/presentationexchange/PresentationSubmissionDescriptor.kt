/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresentationSubmissionDescriptor(
    @SerialName("id")
    val idFromPresentationRequest: String = "",

    val format: String = "",

    val path: String = "",

    @SerialName("path_nested")
    var pathNested: PresentationSubmissionDescriptor? = null
)