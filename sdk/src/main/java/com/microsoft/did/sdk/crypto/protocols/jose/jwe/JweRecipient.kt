package com.microsoft.did.sdk.crypto.protocols.jose.jwe

import com.microsoft.did.sdk.crypto.keys.PublicKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class JweRecipient (
    @SerialName("encrypted_key")
    val encryptedKey: ByteArray,
    @SerialName("header")
    val headers: Map<String, String>,
    @Transient
    val publicKey: PublicKey? = null
) {}