package com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi

data class CryptoKey(
    val type: KeyType,
    val extractable: Boolean,
    val algorithm: Algorithm,
    val usages: List<KeyUsage>,
    val handle: Any?
)