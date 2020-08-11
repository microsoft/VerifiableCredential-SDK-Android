/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.presentationexchange

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialSchema(
    //TODO: temporarily url in request but it should be uri
    @SerialName("url")
    val credentialSchemaUris: List<String>,
    @SerialName("name")
    val credentialSchemaName: String,
    @SerialName("purpose")
    val credentialSchemaReason: String
)