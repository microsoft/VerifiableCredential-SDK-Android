package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.utilities.ILogger

enum class KeyType(val value: String) {
    EllipticCurve("EC"),
    Octets("oct"),
    RSA("RSA")
}

fun toKeyType(kty: String, logger: ILogger): KeyType {
    return when (kty) {
        KeyType.EllipticCurve.value -> KeyType.EllipticCurve
        KeyType.RSA.value -> KeyType.RSA
        KeyType.Octets.value -> KeyType.Octets
        else -> throw logger.error("Unknown Key Type value: $kty")
    }
}