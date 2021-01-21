package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.util.controlflow.KeyException

enum class KeyType(val value: String) {
    EllipticCurve("EC"),
    Octets("oct"),
    RSA("RSA");

    companion object {
        fun fromString(kty: String): KeyType {
            return values().find { it.value == kty } ?: throw KeyException("Unknown Key Type value: $kty")
        }
    }
}