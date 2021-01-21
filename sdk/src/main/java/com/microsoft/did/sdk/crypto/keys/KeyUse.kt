package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.util.controlflow.KeyException

enum class KeyUse(val value: String) {
    Signature("sig"),
    Encryption("enc"),
    Secret("secret");

    companion object {
        fun fromString(use: String): KeyUse {
            return values().find { it.value == use } ?: throw KeyException("Unknown Key Use value: $use")
        }
    }
}