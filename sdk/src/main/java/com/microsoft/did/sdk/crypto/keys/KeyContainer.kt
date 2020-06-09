package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm

class KeyContainer<T : IKeyStoreItem>(
    val kty: KeyType,
    val keys: List<T> = emptyList(),
    val use: KeyUse? = null,
    val alg: Algorithm? = null
) {
    fun getKey(id: String? = null): T {
        return if (id.isNullOrBlank()) {
            keys.first()
        } else {
            keys.first { it.kid == id }
        }
    }
}