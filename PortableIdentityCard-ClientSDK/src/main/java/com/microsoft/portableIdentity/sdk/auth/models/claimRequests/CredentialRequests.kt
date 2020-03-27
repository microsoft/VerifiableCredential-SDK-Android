/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.models.claimRequests

import kotlinx.serialization.Serializable

/**
 * Class to bucket together credential requests.
 */
@Serializable
data class CredentialRequests(
    // IdToken Requests.
    val idTokens: List<IdTokenRequest>? = null,

    // Verifiable Presentation Requests.
    val presentations: List<VerifiablePresentationRequest>? = null,

    // SelfIssued Claim Requests.
    val selfIssued: SelfIssuedClaimRequest? = null
)