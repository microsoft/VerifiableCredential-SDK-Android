package com.microsoft.useragentSdk.crypto.keys

enum class KeyType(keyType: String) {
    EllipticCurve("EC"),
    Octets("oct"),
    RSA("RSA")
}