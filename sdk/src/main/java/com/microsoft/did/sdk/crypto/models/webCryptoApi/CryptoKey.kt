package com.microsoft.did.sdk.crypto.models.webCryptoApi

import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm

data class CryptoKey(
    val type: KeyType,
    val extractable: Boolean,
    val algorithm: Algorithm,
    val usages: List<KeyUsage>,
    val handle: Any?
)