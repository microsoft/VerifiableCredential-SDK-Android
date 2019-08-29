package com.microsoft.did.sdk.crypto.models.webCryptoApi

/**
 * A type of operation that may be performed using a key.
 */
enum class KeyUsage (val value: String) {
    Encrypt("encrypt"),
    Decrypt("decrypt"),
    Sign("sign"),
    Verify("verify"),
    DeriveKey("deriveKey"),
    DeriveBits("deriveBits"),
    WrapKey("wrapKey"),
    UnwrapKey("unwrapKey")
}