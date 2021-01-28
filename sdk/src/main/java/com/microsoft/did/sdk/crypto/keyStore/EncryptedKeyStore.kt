// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.microsoft.did.sdk.util.JavaObjectSerializer
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import java.security.Key
import javax.inject.Inject

class EncryptedKeyStore @Inject constructor(context: Context) : KeyStore() {

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

    override fun saveKey(key: Key, keyId: String) {
        val serializedKey = JavaObjectSerializer.toString(key)
        encryptedSharedPreferences.edit().putString(keyId, serializedKey).apply()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Key> getKey(keyId: String): T {
        val keyString: String = encryptedSharedPreferences.getString(keyId, null)
            ?: throw KeyStoreException("Key $keyId not found")
        return JavaObjectSerializer.fromString(keyString) as? T
            ?: throw KeyStoreException("Stored key $keyId is not of the requested Key type")
    }
}