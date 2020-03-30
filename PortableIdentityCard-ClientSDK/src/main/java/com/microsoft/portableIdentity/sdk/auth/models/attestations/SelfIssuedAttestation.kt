package com.microsoft.portableIdentity.sdk.auth.models.attestations

import kotlinx.serialization.Serializable

@Serializable
data class SelfIssuedAttestation (
    val claims: List<ClaimAttestation>
)