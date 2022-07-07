/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.models

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * Contents of a Verifiable Credential Jws Token.
 *
 * @see [Verifiable Credential Spec](https://www.w3.org/TR/vc-data-model/#basic-concepts)
 */
@Entity
@Serializable
data class VerifiableCredentialContent(

    // ID of the Verifiable Credential.
    val jti: String,

    // Claims and Service information.
    val vc: VerifiableCredentialDescriptor,

    // Subject of the VC (e.g. did owned by the user.)
    val sub: String,

    // Issuer of the VC (e.g. did owned by the issuer.)
    val iss: String,

    // When the token was signed.
    val iat: Long,

    // When the token expires.
    val exp: Long? = null,

    // optional parameter.
    val wrn: String = ""
)