package com.microsoft.useragentSdk.crypto.keys

enum class KeyType(val value: String) {
    EllipticCurve("EC"),
    Octets("oct"),
    RSA("RSA")
}