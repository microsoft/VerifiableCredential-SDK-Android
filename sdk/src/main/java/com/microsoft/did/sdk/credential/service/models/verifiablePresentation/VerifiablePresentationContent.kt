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

    @SerialName("jti")
    val vpId: String,

    val purpose: String = "verify",

    @SerialName("vp")
    val verifiablePresentation: VerifiablePresentationDescriptor,

    // Issuer of the VP (e.g. did owned by the user, SIOP.did = VP.iss)
    @SerialName("iss")
    val issuerOfVp: String,

    @SerialName("iat")
    val tokenIssuedTime: Long = 0,

    @SerialName("exp")
    val tokenExpiryTime: Long = 0,

    @SerialName("nbf")
    val tokenNotValidBefore: Long = 0,

    @SerialName("aud")
    val audience: String,

    @SerialName("wrn")
    val warning: String = ""
)