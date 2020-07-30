/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CredentialSchema(
    @SerialName("uri")
    val credentialSchemaUriList: List<String>,
    @SerialName("name")
    val credentialSchemaName: String,
    @SerialName("purpose")
    val credentialSchemaReason: String
)