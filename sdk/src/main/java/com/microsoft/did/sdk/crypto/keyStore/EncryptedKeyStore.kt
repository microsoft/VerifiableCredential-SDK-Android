// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import com.nimbusds.jose.jwk.JWK
import javax.inject.Inject

class EncryptedKeyStore @Inject constructor(context: Context) {

    companion object {
        private const val KEY_PREFIX = "DID_KEY_"
        private const val FILE_NAME = "DID_encrypted_keys"
    }

    private val encryptedSharedPreferences by lazy { getSharedPreferences(context) }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun storeKey(keyId: String, key: JWK) {
        encryptedSharedPreferences.edit().putString(KEY_PREFIX + keyId, key.toJSONString()).apply()
    }

    fun getKey(keyId: String): JWK {
        val keyJson = encryptedSharedPreferences.getString(KEY_PREFIX + keyId, null)
            ?: throw KeyStoreException("Key $keyId not found")
        return JWK.parse(keyJson)
    }

    fun containsKey(keyId: String): Boolean {
        return encryptedSharedPreferences.contains(KEY_PREFIX + keyId)
    }
}