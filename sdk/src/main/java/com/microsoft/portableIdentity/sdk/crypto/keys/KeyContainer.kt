package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.Algorithm

class KeyContainer<T: IKeyStoreItem> (val kty: KeyType, val keys: List<T> = emptyList(), val use: KeyUse? = null, val alg: Algorithm? = null) {
    fun getKey(id: String? = null): T {
        return if (id.isNullOrBlank()) {
            keys.first()
        } else {
            keys.first { it.kid == id }
        }
    }
}