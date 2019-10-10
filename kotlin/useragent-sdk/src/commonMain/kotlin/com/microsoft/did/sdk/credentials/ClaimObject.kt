package com.microsoft.did.sdk.credentials

import kotlinx.serialization.Serializable

@Serializable
data class ClaimObject(val claimClass: String,
                       val claimDescriptions: List<ClaimDescription>,
                       val claimIssuer: String,
                       val claimDetails: List<ClaimDetail>)