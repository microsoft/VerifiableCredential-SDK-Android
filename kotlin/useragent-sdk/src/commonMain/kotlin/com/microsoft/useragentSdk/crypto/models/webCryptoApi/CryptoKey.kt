package com.microsoft.useragentSdk.crypto.models.webCryptoApi

interface CryptoKey {
    val type: KeyType
    val extractable: Boolean
    val algorithm: Algorithm
    val usages: List<KeyUsage>
    val handle: Any?
};