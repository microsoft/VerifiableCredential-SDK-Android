package com.microsoft.useragentSdk.crypto.models.webCryptoApi

interface CryptoKey {
    val type: KeyType
    val extractable: Boolean
    val algorithm: Map<String, Any>
    val usages: List<KeyUsage>
    val handle: Any?
};