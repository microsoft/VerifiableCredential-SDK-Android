package com.microsoft.did.sdk.credential.service.models.attestations

import kotlinx.serialization.Serializable

/**
 * EXPERIMENTAL
 */
@Serializable
data class SelfIssuedAttestation(
    val claims: List<ClaimAttestation> = emptyList()
)