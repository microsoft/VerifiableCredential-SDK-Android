/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialPresentationSubmissionDescriptor(
    val id: String,
    @SerialName("path")
    val credentialPath: String
) {
    @SerialName("format")
    var credentialFormat: String = ""
    @SerialName("encoding")
    var credentialEncoding: String = ""
}