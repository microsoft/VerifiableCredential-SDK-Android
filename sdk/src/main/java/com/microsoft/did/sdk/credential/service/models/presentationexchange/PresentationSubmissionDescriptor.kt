/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.Serializable

@Serializable
data class PresentationSubmissionDescriptor(
    val idFromPresentationRequest: String = "",
    val path: String = "",
    val format: String,
    val encoding: String = ""
)