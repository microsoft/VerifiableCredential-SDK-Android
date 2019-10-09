package com.microsoft.did.sdk.auth

import kotlinx.serialization.Serializable

@Serializable
data class IdToken {
    val gender: String?
}