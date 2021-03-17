/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contents of an OpenID Self-Issued Token Response.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
data class PresentationResponseClaims(
    @SerialName("presentation_submission")
    val presentationSubmission: PresentationSubmission = PresentationSubmission(emptyList()),

    val attestations: AttestationClaimModel = AttestationClaimModel(),

    var state: String = "",

    var nonce: String = ""
) : OidcResponseClaims()