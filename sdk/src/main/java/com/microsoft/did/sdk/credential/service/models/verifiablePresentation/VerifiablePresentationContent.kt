/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.verifiablePresentation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contents of a Verifiable Presentation Jws Token.
 *
 * @see [Verifiable Credential Spec](https://www.w3.org/TR/vc-data-model/#basic-concepts)
 */
@Serializable
data class VerifiablePresentationContent(

    // ID of the Verifiable Credential.
    val jti: String,

    // purpose of presentation
    val purpose: String = "verify",

    val vp: VerifiablePresentationDescriptor,

    // Issuer of the VP (e.g. did owned by the user, SIOP.did = VP.iss)
    val iss: String,

    @SerialName("iat")
    val tokenIssuedTime: Long = 0,

    @SerialName("exp")
    val tokenExpiryTime: Long = 0,

    @SerialName("nbf")
    val tokenNotValidBefore: Long = 0,

    // audience of the request
    val aud: String,

    // optional parameter.
    val wrn: String = ""
)