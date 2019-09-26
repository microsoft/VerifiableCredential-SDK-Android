package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm

class KeyContainer<T> (val kty: KeyType, val use: KeyUse?, val alg: Algorithm?, val keys: List<T> = emptyList()) {
    fun getKey(): T {
        return keys.last()
    }
}