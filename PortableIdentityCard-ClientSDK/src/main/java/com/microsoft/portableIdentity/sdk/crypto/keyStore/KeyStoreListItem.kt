package com.microsoft.portableIdentity.sdk.crypto.keyStore

import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType

data class KeyStoreListItem (val kty: KeyType, val kids: MutableList<String>) {
    fun getLatestKeyId(): String {
        return kids.first()
    }
}