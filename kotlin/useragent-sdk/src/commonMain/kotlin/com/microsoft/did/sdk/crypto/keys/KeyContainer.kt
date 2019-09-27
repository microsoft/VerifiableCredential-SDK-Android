package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm

class KeyContainer<T> (val kty: KeyType, val keys: List<T> = emptyList(), val use: KeyUse? = null, val alg: Algorithm? = null) {
    fun getKey(): T {
        return keys.last()
    }
}