package com.microsoft.portableIdentity.sdk.credentials

import kotlinx.serialization.Serializable

@Serializable
data class ClaimDescription (val header: String, val body: String)