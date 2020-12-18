package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import kotlinx.serialization.Serializable

@Serializable
data class JweGeneralJson (
    val protected: String?,
    val unprotected: Map<String, String>?,
    val iv: String,
    val aad: String,
    val ciphertext: String,
    val tag: String,
    val recipients: List<JweRecipient>
)
{}