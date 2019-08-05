package com.microsoft.useragentSdk.crypto.models

/**
 * A type of operation that may be performed using a key.
 */
enum class KeyUsage (val keyUsage: String) {
    Encrypt("encrypt"),
    Decrypt("decrypt"),
    Sign("sign"),
    Verify("verify"),
    DeriveKey("deriveKey"),
    DeriveBits("deriveBits"),
    WrapKey("wrapKey"),
    UnwrapKey("unwrapKey")
}