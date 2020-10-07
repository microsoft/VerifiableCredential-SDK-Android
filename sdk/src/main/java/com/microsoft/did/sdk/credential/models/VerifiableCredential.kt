/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.models

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class VerifiableCredential(
    // jti of the verifiable credential
    val jti: String,

    // raw token.
    val raw: String,

    // contents of the Verifiable Credential token.
    val contents: VerifiableCredentialContent
)