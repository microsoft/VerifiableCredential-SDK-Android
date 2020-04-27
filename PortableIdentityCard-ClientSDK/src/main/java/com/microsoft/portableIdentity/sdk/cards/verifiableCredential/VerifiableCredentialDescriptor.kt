/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.cards.verifiableCredential

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data model to describe a Verifiable Credential.
 */
@Serializable
data class VerifiableCredentialDescriptor(
        @SerialName("@context")
    val context: List<String>,

    val type: List<String>,

    val credentialSubject: Map<String, String>,

    val credentialStatus: ServiceDescriptor? = null,

    val revokeService: ServiceDescriptor? = null
)