/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.cards.verifiableCredential

import androidx.room.Entity
import kotlinx.serialization.Serializable

/**
 * Contents of a Verifiable Presentation Jws Token.
 *
 * @see [Verifiable Credential Spec](https://www.w3.org/TR/vc-data-model/#basic-concepts)
 */
@Serializable
data class VerifiablePresentationContent (

    // ID of the Verifiable Credential.
    val jti: String,

    // purpose of presentation
    val purpose: String = "verify",

    val vp: VerifiablePresentationDescriptor,

    // Issuer of the VP (e.g. did owned by the user, SIOP.did = VP.iss)
    val iss: String,

    // When the token was signed.
    val iat: Long,

    // When the token expires.
    val exp: Long,

    val nbf: Long? = null,

    // audience of the request
    val aud: String,

    // optional parameter.
    val wrn: String = ""
)