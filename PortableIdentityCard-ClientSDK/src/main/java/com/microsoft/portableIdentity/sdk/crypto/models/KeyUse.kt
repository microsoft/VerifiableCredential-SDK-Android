package com.microsoft.portableIdentity.sdk.crypto.models

enum class KeyUse(val value: String) {
    Signature("sig"),
    Encryption("enc")
}

fun toKeyUse(use: String): KeyUse? {
    return when (use) {
        KeyUse.Signature.value -> KeyUse.Signature
        KeyUse.Encryption.value -> KeyUse.Encryption
        else -> null
    }
}