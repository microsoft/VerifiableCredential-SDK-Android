// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.nimbusds.jose.util.Base64URL
import java.security.KeyStore
import java.security.SecureRandom
import javax.inject.Inject

object PasswordlessKeyStore @Inject constructor(context: Context) {
    private val keyName = "DID_KEYVAULT_LOCK"
    private val keyStoreFileName = "didKeyStore.jks"
    val keyStore: KeyStore

    init {
        val encryptedSharedPreferences = getSharedPreferences(context)
        val password = getOrGenerateKey(encryptedSharedPreferences)
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(context.openFileInput(keyStoreFileName), password)
    }

    fun save(context: Context) {
        val encryptedSharedPreferences = getSharedPreferences(context)
        val password = getOrGenerateKey(encryptedSharedPreferences)
        keyStore.store(context.openFileOutput(keyStoreFileName, Context.MODE_PRIVATE), password)
    }

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

    private fun getOrGenerateKey(encryptedSharedPreferences: SharedPreferences): CharArray {
        var key = encryptedSharedPreferences.getString(keyName, null)
        if (key == null) {
            key = generateSecret()
            encryptedSharedPreferences.edit().putString(keyName, key).apply()
        }
        return key.toCharArray()
    }

    private fun generateSecret(): String {
        val byteArray = ByteArray(32)
        SecureRandom().nextBytes(byteArray)
        return Base64URL.encode(byteArray).toString()
    }
}