/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.Serializable

/**
 * Contents of an OpenID Self-Issued Token Response.
 *
 * @see [OpenID Spec](https://openid.net/specs/openid-connect-core-1_0.html#JWTRequests)
 */
@Serializable
data class IssuanceResponseClaims(
    var contract: String = "",

    var attestations: AttestationClaimModel = AttestationClaimModel()
) : OidcResponseClaims()