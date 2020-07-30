/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class CredentialPresentationDescriptors(
    val id: String,
    val schema: CredentialSchema,
    val credentialIssuance: List<CredentialIssuanceMetadata>
)