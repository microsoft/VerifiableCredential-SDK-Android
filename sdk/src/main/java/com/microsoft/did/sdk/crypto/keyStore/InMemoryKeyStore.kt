package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.Key
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class InMemoryKeyStore(private val serializer: Json) : KeyStore() {
    private val keys: MutableMap<String, String> = mutableMapOf()

    override fun saveKey(key: Key) {
        val serializedKey = serializer.encodeToString(key)
        keys[key.kid] = serializedKey
    }


    override fun getKey(keyId: String): Key {
        val keyString: String = encryptedSharedPreferences.getString(keyId, null)
                ?: throw KeyStoreException("Key $keyId not found")
        return serializer.decodeFromString(st, keyString)
    }
}