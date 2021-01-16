package com.microsoft.did.sdk.crypto.models.webCryptoApi

import com.microsoft.did.sdk.util.controlflow.KeyException

/**
 * A type of operation that may be performed using a key.
 */
enum class KeyUsage(val value: String) {
    Encrypt("encrypt"),
    Decrypt("decrypt"),
    Sign("sign"),
    Verify("verify"),
    DeriveKey("deriveKey"),
    DeriveBits("deriveBits"),
    WrapKey("wrapKey"),
    UnwrapKey("unwrapKey");

    companion object {
        fun fromString(keyUsage: String): KeyUsage {
            return values().find { it.value == keyUsage } ?: throw KeyException("Unknown Key Usage value: $keyUsage")
        }
    }
}