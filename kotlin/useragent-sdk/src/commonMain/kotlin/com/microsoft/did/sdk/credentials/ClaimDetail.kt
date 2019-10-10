package com.microsoft.did.sdk.credentials

import kotlinx.serialization.Serializable

@Serializable
data class ClaimDetail (
    val type: String = JWS,
    val data: String
) {
    companion object {
        const val UNSIGNED = "unsigned"
        const val JWS = "jws"
    }
}