package com.microsoft.did.sdk.credentials

import kotlinx.serialization.Serializable

@Serializable
data class ClaimDetail (
    val type: String = "jws",
    val data: String
) {
}