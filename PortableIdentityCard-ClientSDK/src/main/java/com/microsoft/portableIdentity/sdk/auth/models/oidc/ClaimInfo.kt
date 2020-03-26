package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class ClaimInfo (
    // name of the claim.
    val claim: String,

    // if claim is required
    val required: Boolean? = null,

    // type of object the claim should be.
    val type: String? = null
)