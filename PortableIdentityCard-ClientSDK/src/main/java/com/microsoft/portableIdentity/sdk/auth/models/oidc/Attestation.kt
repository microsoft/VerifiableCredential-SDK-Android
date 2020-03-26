package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class Attestation(
    // IdToken Requests.
    val idTokens: Map<String, String>? = null,

    // Verifiable Presentation Requests.
    val presentations: List<Presentation>? = null,

    // SelfIssued Claim Requests.
    val selfIssued: SelfIssued? = null
)