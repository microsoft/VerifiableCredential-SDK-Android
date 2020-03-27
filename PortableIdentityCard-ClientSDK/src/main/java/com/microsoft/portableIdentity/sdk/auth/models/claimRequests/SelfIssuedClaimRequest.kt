package com.microsoft.portableIdentity.sdk.auth.models.claimRequests

import com.microsoft.portableIdentity.sdk.auth.models.claimRequests.ClaimInfo
import kotlinx.serialization.Serializable

@Serializable
data class SelfIssuedClaimRequest (
    val claims: List<ClaimInfo>
)