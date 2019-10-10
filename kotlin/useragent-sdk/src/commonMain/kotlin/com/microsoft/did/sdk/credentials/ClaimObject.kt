package com.microsoft.did.sdk.credentials

import kotlinx.serialization.Serializable

@Serializable
data class ClaimObject(val claimClass: String,
                       val claimDescriptions: List<ClaimDescription>,
                       val issuer: String,
                       val claimObjects: List<ClaimObject>?,
                       val claimDetails: List<ClaimDetail>,
                       val signedClaimDetails: List<String>?)