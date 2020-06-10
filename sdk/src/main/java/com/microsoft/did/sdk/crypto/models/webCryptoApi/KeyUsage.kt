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
    UnwrapKey("unwrapKey")
}

fun toKeyUsage(key_ops: String): KeyUsage {
    return when (key_ops) {
        KeyUsage.Encrypt.value -> KeyUsage.Encrypt
        KeyUsage.Decrypt.value -> KeyUsage.Decrypt
        KeyUsage.Sign.value -> KeyUsage.Sign
        KeyUsage.Verify.value -> KeyUsage.Verify
        KeyUsage.DeriveBits.value -> KeyUsage.DeriveBits
        KeyUsage.DeriveKey.value -> KeyUsage.DeriveKey
        KeyUsage.WrapKey.value -> KeyUsage.WrapKey
        KeyUsage.UnwrapKey.value -> KeyUsage.UnwrapKey
        else -> throw KeyException("Unknown key_op $key_ops")
    }
}