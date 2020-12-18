package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JweFlatJson (
    val protected: String?,
    val unprotected: Map<String, String>?,
    val header: Map<String, String>?,
    @SerialName("encrypted_key")
    val encryptedKey: String,
    val iv: String,
    val aad: String,
    val ciphertext: String,
    val tag: String,
)