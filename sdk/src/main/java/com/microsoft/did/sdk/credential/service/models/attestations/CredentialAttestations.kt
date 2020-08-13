/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

/**
 * Class to bucket together credential requests.
 */
@Serializable
data class CredentialAttestations(
    // IdToken Requests.
    val idTokens: List<IdTokenAttestation> = emptyList(),

    // Verifiable Presentation Requests.
    val presentations: List<PresentationAttestation> = emptyList(),

    // SelfIssued Claim Requests.
    val selfIssued: SelfIssuedAttestation = SelfIssuedAttestation()
)