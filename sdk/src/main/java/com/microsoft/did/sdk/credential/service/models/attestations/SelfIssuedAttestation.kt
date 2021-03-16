package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

/**
 * EXPERIMENTAL
 */
@Serializable
data class SelfIssuedAttestation(
    val claims: List<ClaimAttestation> = emptyList(),

    // True, if presentation is required.
    val required: Boolean = false,

    val encrypted: Boolean = false
)