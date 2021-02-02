// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.microsoft.did.sdk.util.JavaObjectSerializer
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import java.security.Key
import java.security.KeyPair
import javax.inject.Inject

class EncryptedKeyStore @Inject constructor(context: Context) {

    companion object {
        private const val KEY_PREFIX = "DID_KEY_"
        private const val KEYPAIR_PREFIX = "DID_KEYPAIR_"
    }

    private val encryptedSharedPreferences = getSharedPreferences(context)

    private fun getSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun storeKeyPair(keyPair: KeyPair, keyId: String) {
        val serializedKey = JavaObjectSerializer.toString(keyPair)
        encryptedSharedPreferences.edit().putString(KEYPAIR_PREFIX + keyId, serializedKey).apply()
    }

    fun getKeyPair(keyId: String): KeyPair {
        val keyString = encryptedSharedPreferences.getString(keyId, null)
            ?: throw KeyStoreException("Key $keyId not found")
        return JavaObjectSerializer.fromString(keyString) as KeyPair
    }

    fun storeKey(key: Key, keyId: String) {
        val serializedKey = JavaObjectSerializer.toString(key)
        encryptedSharedPreferences.edit().putString(KEY_PREFIX + keyId, serializedKey).apply()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Key> getKey(keyId: String): T {
        val keyString = encryptedSharedPreferences.getString(keyId, null)
            ?: throw KeyStoreException("Key $keyId not found")
        return JavaObjectSerializer.fromString(keyString) as? T
            ?: throw KeyStoreException("Stored key $keyId is not of the requested Key type")
    }
}