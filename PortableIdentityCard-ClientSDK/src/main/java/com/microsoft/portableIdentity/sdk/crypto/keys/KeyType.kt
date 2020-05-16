package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.utilities.controlflow.KeyException

enum class KeyType(val value: String) {
    EllipticCurve("EC"),
    Octets("oct"),
    RSA("RSA")
}

fun toKeyType(kty: String): KeyType {
    return when (kty) {
        KeyType.EllipticCurve.value -> KeyType.EllipticCurve
        KeyType.RSA.value -> KeyType.RSA
        KeyType.Octets.value -> KeyType.Octets
        else -> throw KeyException("Unknown Key Type value: $kty")
    }
}