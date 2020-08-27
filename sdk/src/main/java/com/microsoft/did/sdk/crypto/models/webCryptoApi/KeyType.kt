package com.microsoft.did.sdk.crypto.models.webCryptoApi

/**
 * The type of a key.
 */
enum class KeyType(val value: String) {
    Public("public"),
    Private("private"),

    /** Opaque keying material, including that used for symmetric algorithms */
    Secret("secret")
}