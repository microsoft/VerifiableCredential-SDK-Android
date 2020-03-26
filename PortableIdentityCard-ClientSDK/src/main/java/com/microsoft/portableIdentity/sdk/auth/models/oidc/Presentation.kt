package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class Presentation(
    val required: Boolean,
    val credentialType: String,
    val issuers: Array<AcceptedIssuer>,
    val contracts: Array<String>? = null
)