package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class SelfIssued (
    val claims: List<ClaimInfo>
)