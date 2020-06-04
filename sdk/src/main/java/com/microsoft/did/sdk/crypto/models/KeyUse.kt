package com.microsoft.did.sdk.crypto.models

enum class KeyUse(val value: String) {
    Signature("sig"),
    Encryption("enc"),
    Secret("secret")
}

fun toKeyUse(use: String): KeyUse? {
    return when (use) {
        KeyUse.Signature.value -> KeyUse.Signature
        KeyUse.Encryption.value -> KeyUse.Encryption
        KeyUse.Secret.value -> KeyUse.Secret
        else -> null
    }
}