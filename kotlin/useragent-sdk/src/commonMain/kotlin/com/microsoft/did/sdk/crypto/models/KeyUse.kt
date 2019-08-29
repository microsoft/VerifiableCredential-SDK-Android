package com.microsoft.did.sdk.crypto.models

enum class KeyUse(val value: String) {
    Signature("sig"),
    Encryption("enc")
}