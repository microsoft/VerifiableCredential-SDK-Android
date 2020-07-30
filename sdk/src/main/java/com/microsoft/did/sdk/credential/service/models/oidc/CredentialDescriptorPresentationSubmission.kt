/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialDescriptorPresentationSubmission(
    val id: String,
    @SerialName("format")
    val credentialFormat: String,
    @SerialName("encoding")
    val credentialEncoding: String,
    @SerialName("path")
    val credentialPath: String
)