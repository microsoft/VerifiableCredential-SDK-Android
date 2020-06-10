package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.KeyType

data class KeyStoreListItem(val kty: KeyType, val kids: MutableList<String>) {
    fun getLatestKeyId(): String {
        return kids.first()
    }
}