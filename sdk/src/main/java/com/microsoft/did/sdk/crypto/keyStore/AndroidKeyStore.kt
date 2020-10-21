// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.crypto.keyStore

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.keys.toKeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.util.AndroidKeyConverter
import com.microsoft.did.sdk.util.byteArrayToString
import com.microsoft.did.sdk.util.controlflow.KeyException
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.stringToByteArray
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidKeyStore @Inject constructor(private val context: Context, private val serializer: Json) :
    com.microsoft.did.sdk.crypto.keyStore.KeyStore() {

    companion object {
        const val provider = "AndroidKeyStore"
        private val regexForKeyReference = Regex("^#(.*)_[^.]+$")
        private val regexForKeyIndex = Regex("^#.*_([^.]+\$)")

        val keyStore: KeyStore by lazy { KeyStore.getInstance(provider).apply { load(null) } }
    }

    override fun getSecretKey(keyReference: String): KeyContainer<SecretKey> {
        val softwareKeys = listSecureData()
        val key = softwareKeys[keyReference] ?: throw KeyStoreException("Key $keyReference not found")
        if (key.kty != KeyType.Octets) {
            throw KeyException("Key $keyReference (type ${key.kty.value}) is not a secret.")
        }

        return KeyContainer(
            kty = key.kty,
            keys = key.kids.map {
                getSecureSecretKey(it)!!
            }
        )
    }

    override fun getPrivateKey(keyReference: String): KeyContainer<PrivateKey> {
        val nativeKeys = listNativeKeys()
        var key = nativeKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map {
                    AndroidKeyConverter.androidPrivateKeyToPrivateKey(it, keyStore)
                }
            )
        }
        val softwareKeys = listSecureData()
        key = softwareKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map {
                    getSecurePrivateKey(it)!!
                }
            )
        }
        throw KeyStoreException("Key $keyReference not found")
    }

    override fun getPublicKey(keyReference: String): KeyContainer<PublicKey> {
        val nativeKeys = listNativeKeys()
        var key = nativeKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map {
                    val entry = keyStore.getCertificate(it).publicKey
                        ?: throw KeyException("Key $it is not a private key.")
                    AndroidKeyConverter.androidPublicKeyToPublicKey(it, entry)
                }
            )
        }
        val softwareKeys = listSecureData()
        key = softwareKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map {
                    getSecurePublicKey(it)!!
                }
            )
        }
        throw KeyStoreException("Key $keyReference not found")
    }

    override fun getSecretKeyById(keyId: String): SecretKey? {
        val keyRef = findReferenceInList(this.list(), keyId)
        return if (!keyRef.isNullOrBlank()) {
            getSecretKey(keyRef).keys.firstOrNull { it.kid == keyId }
        } else {
            null
        }
    }

    override fun getPrivateKeyById(keyId: String): PrivateKey? {
        val nativeKeys = listNativeKeys()
        var keyRef = findReferenceInList(nativeKeys, keyId)
        if (!keyRef.isNullOrBlank()) { // This keyID exists within the android keystore
            return AndroidKeyConverter.androidPrivateKeyToPrivateKey(keyId, keyStore)
        }
        val softwareKeys = listSecureData()
        keyRef = findReferenceInList(softwareKeys, keyId)
        if (!keyRef.isNullOrBlank()) {
            return getSecurePrivateKey(keyId)
        }
        return null
    }

    override fun getPublicKeyById(keyId: String): PublicKey? {
        val nativeKeys = listNativeKeys()
        var keyRef = findReferenceInList(nativeKeys, keyId)
        if (!keyRef.isNullOrBlank()) { // This keyID exists within the android keystore
            val entry = keyStore.getCertificate(keyId).publicKey ?: throw KeyException("Key $keyId is not a private key.")
            return AndroidKeyConverter.androidPublicKeyToPublicKey(keyId, entry)
        }
        val softwareKeys = listSecureData()
        keyRef = findReferenceInList(softwareKeys, keyId)
        if (!keyRef.isNullOrBlank()) {
            return getSecurePublicKey(keyId)
        }
        return null
    }

    fun deletePrivateKey(keyId: String) {
        val nativeKeys = listNativeKeys()
        var keyRef = findReferenceInList(nativeKeys, keyId)
        if (!keyRef.isNullOrBlank()) {
            keyStore.deleteEntry(keyId)
            return
        }
        val softwareKeys = listSecureData()
        keyRef = findReferenceInList(softwareKeys, keyId)
        if (!keyRef.isNullOrBlank()) {
            deleteSecureData(keyId)
        }
    }

    private fun findReferenceInList(list: Map<String, KeyStoreListItem>, keyId: String): String? {
        return list.filter {
            it.value.kids.contains(keyId)
        }.entries.firstOrNull()?.key
    }

    override fun save(keyReference: String, key: SecretKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        val jwk = key.toJWK()
        jwk.kid = alias
        val jwkString = serializer.encodeToString(JsonWebKey.serializer(), jwk)
        val keyValue = stringToByteArray(jwkString)
        saveSecureData(alias, keyValue)
    }

    override fun save(keyReference: String, key: PrivateKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        if (keyStore.containsAlias(alias)) {
            // do nothing, the key is already there.
            return
        }
        // This key is not natively supported
        val jwk = key.toJWK()
        jwk.kid = alias
        val jwkString = serializer.encodeToString(JsonWebKey.serializer(), jwk)
        val keyValue = stringToByteArray(jwkString)
        saveSecureData(alias, keyValue)
    }

    override fun save(keyReference: String, key: PublicKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        if (keyStore.containsAlias(alias)) {
            // do nothing, the key is already there.
            return
        }
        throw KeyException("Why are you even saving a public key; this makes no sense. Rethink your life.")
    }

    override fun list(): Map<String, KeyStoreListItem> {
        val result = mutableMapOf<String, KeyStoreListItem>()
        result.putAll(listSecureData())
        result.putAll(listNativeKeys())
        return result
    }

    private fun listNativeKeys(): Map<String, KeyStoreListItem> {
        val output = emptyMap<String, KeyStoreListItem>().toMutableMap()
        val aliases = keyStore.aliases()
        // KeyRef (as key reference) -> KeyRef.VersionNumber (as key identifier)
        for (alias in aliases) {
            if (alias.matches(regexForKeyReference)) {
                val entry = keyStore.getCertificate(alias)
                val matches = regexForKeyReference.matchEntire(alias)
                val values = matches!!.groupValues

                // Get the keyType associated with this key.
                val kty: KeyType = AndroidKeyConverter.whatKeyTypeIs(entry.publicKey)

                // Add the key to an ListItem or make a new one
                if (output.containsKey(values[1])) {
                    val listItem = output[values[1]]!!
                    if (listItem.kty != kty) {
                        throw KeyStoreException(
                            "Key Container ${values[1]} contains keys of two different " +
                                "types (${listItem.kty.value}, ${kty.value})"
                        )
                    }
                    listItem.kids.add(alias)
                } else {
                    output[values[1]] = KeyStoreListItem(kty, mutableListOf(alias))
                }
            }
        }
        return output
    }

    private fun listSecureData(): Map<String, KeyStoreListItem> {
        val sharedPreferences = getSharedPreferences()
        val keys = sharedPreferences.all.keys
        // all stored keys should be in JWT format
        val keyMap = mutableMapOf<String, KeyStoreListItem>()
        keys.forEach {
            // verify that it matches the regex and grab the key reference
            val keyReferenceMatch = AndroidKeyStore.regexForKeyReference.matchEntire(it)
            if (keyReferenceMatch != null) {
                val keyRef = keyReferenceMatch.groupValues[1]
                val jwkBase64 = sharedPreferences.getString(it, null)!!
                val jwkData = Base64.decode(jwkBase64, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                val key = serializer.decodeFromString(JsonWebKey.serializer(), byteArrayToString(jwkData))
                val keyType = toKeyType(key.kty)
                if (!keyMap.containsKey(keyRef)) {
                    keyMap[keyRef] = KeyStoreListItem(keyType, mutableListOf(it))
                } else {
                    val listItem = keyMap[keyRef]!!
                    if (keyType != listItem.kty) {
                        throw KeyStoreException("Key $keyRef has two different key types (${keyType.value}, ${listItem.kty.value})")
                    }
                    listItem.kids.add(it)
                    keyMap[keyRef] = listItem
                }
            }
        }
        return keyMap
    }

    private fun getSecurePublicKey(alias: String): PublicKey? {
        return getSecurePrivateKey(alias)?.getPublicKey()
    }

    private fun getSecurePrivateKey(alias: String): PrivateKey? {
        val data = getSecureData(alias) ?: return null
        val jwk = serializer.decodeFromString(JsonWebKey.serializer(), byteArrayToString(data))
        if (jwk.kty == KeyType.RSA.value) {
            return RsaPrivateKey(jwk)
        } else if (jwk.kty == KeyType.EllipticCurve.value) {
            return EllipticCurvePrivateKey(jwk)
        } else {
            throw KeyException("Unknown key type ${jwk.kty}")
        }
    }

    private fun getSecureSecretKey(alias: String): SecretKey? {
        val data = getSecureData(alias) ?: return null
        val jwk = serializer.decodeFromString(JsonWebKey.serializer(), byteArrayToString(data))
        if (jwk.kty != KeyType.Octets.value) {
            throw KeyException("$alias is not a secret key.")
        }
        return SecretKey(jwk)
    }

    private fun getSecureData(alias: String): ByteArray? {
        val sharedPreferences = getSharedPreferences()
        val base64UrlEncodedData = sharedPreferences.getString(alias, null)
        if (base64UrlEncodedData != null) {
            return Base64.decode(base64UrlEncodedData, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        }
        return null
    }

    private fun deleteSecureData(alias: String) {
        val sharedPreferences = getSharedPreferences()
        val editor = sharedPreferences.edit()
        editor.remove(alias)
        editor.apply()
    }

    private fun saveSecureData(alias: String, data: ByteArray) {
        val sharedPreferences = getSharedPreferences()
        val editor = sharedPreferences.edit()
        editor.putString(alias, Base64.encodeToString(data, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        editor.apply()
    }

    private fun getSharedPreferences(): SharedPreferences {
        val masterKeyAlias = getSecretVaultMasterKey()
        return EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getSecretVaultMasterKey(): String {
        val alias = "ms-useragent-secret-masterkey"

        if (!keyStore.containsAlias(alias)) {
            // Generate the master key
            val generator = KeyGenerator.getInstance("AES", provider)
            generator.init(
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generator.generateKey()
        }

        return alias
    }

    //TODO: Modify kid derived from key reference to not use . and not begin with #. It should be Base64url charset for sidetree registration to work with these as ids
    fun checkOrCreateKeyId(keyReference: String, kid: String?): String {
        if (!kid.isNullOrBlank() && !kid.startsWith(keyReference) && !kid.startsWith("#$keyReference")) {
            throw KeyStoreException("Key ID must begin with key reference")
            // This could be relaxed later if we flush keys and use a format of
            // KEYREFERENCE.KEYID and ensure KEYID does not contain the dot delimiter
        }
        return if (!kid.isNullOrBlank()) {
            SdkLog.d("Using key $kid")
            kid
        } else {
            // generate a key id
            val listItem = this.list()[keyReference]
            if (listItem == null) { // no previous keys
                SdkLog.d("New key reference #${keyReference}_1")
                "#${keyReference}_1"
            } else {
                // heuristic, find the last digit and count up
                var latestVersion = listItem.kids.reduce { acc: String, current: String ->
                    val currentValue = regexForKeyIndex.matchEntire(current)?.groupValues?.get(1)?.toInt()
                    val accValue = acc.toIntOrNull()
                    if (currentValue != null && accValue == null) {
                        current
                    } else if (currentValue != null && accValue != null && currentValue > accValue) {
                        current
                    } else {
                        acc
                    }
                }.toIntOrNull() ?: 0

                latestVersion++
                SdkLog.d("New key reference #${keyReference}_$latestVersion")
                "#${keyReference}_$latestVersion"
            }
        }
    }
}