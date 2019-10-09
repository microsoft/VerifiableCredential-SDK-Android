package com.microsoft.did.sdk.credentials

data class ClaimObject(val claimClass: String,
                       val issuer: String,
                       val claimDescriptions: List<ClaimDescription>,
                       val IClaimDetails: List<IClaim>,
                       val signedClaimDetails: List<String>?)