package com.microsoft.did.sdk.crypto.keyStore

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import android.util.Base64
import com.microsoft.did.sdk.crypto.keys.*
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.utilities.stringToByteArray
import kotlinx.serialization.json.Json
import java.security.KeyStore
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.spec.SecretKeySpec
import 	androidx.security.crypto.EncryptedSharedPreferences
import com.microsoft.did.sdk.utilities.AndroidKeyConverter
import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.byteArrayToString
import kotlinx.serialization.parse
import javax.crypto.KeyGenerator

class AndroidKeyStore(private val context: Context): IKeyStore {

    companion object {
        const val provider = "AndroidKeyStore"
        private val regexForKeyReference = Regex("(^.*)\\.[^.]+$")
        private val regexForKeyIndex = Regex("^.*\\.([^.]+$)")

        val keyStore: KeyStore = KeyStore.getInstance(provider).apply {
            load(null)
        }
    }

    override fun getSecretKey(keyReference: String): KeyContainer<SecretKey> {
        val allKeys = this.list()
        val key = allKeys[keyReference] ?: throw Error("Key $keyReference not found")
        if (key.kty != KeyType.Octets) {
            throw Error("Key $keyReference (type ${key.kty.value}) is not a secret.")
        }
        return KeyContainer(
            kty = key.kty,
            keys = key.kids.map {
                AndroidKeyConverter.androidSecretKeyToSecretKey(it,
                    keyStore.getEntry(it, null) as? KeyStore.SecretKeyEntry ?: throw Error("Key $it is not a secret key."))
            }
        )
    }

    override fun getPrivateKey(keyReference: String): KeyContainer<PrivateKey> {
        val nativeKeys = listNativeKeys()
        var key = nativeKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map{
                    AndroidKeyConverter.androidPrivateKeyToPrivateKey(it,
                        keyStore.getEntry(it, null) as? KeyStore.PrivateKeyEntry ?: throw Error("Key $it is not a private key."))
                }
            )
        }
        val softwareKeys = listSecureData()
        key = softwareKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map{
                    getSecurePrivateKey(it)!!
                }
            )
        }
        throw Error("Key $keyReference not found")
    }

    override fun getPublicKey(keyReference: String): KeyContainer<PublicKey> {
        val nativeKeys = listNativeKeys()
        var key = nativeKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map {
                    val entry = keyStore.getEntry(it, null) as? KeyStore.PrivateKeyEntry
                        ?: throw Error("Key $it is not a private key.")
                    AndroidKeyConverter.androidPublicKeyToPublicKey(it, entry.certificate.publicKey)
                }
            )
        }
        val softwareKeys = listSecureData()
        key = softwareKeys[keyReference]
        if (key != null) {
            return KeyContainer(
                kty = key.kty,
                keys = key.kids.map{
                    getSecurePublicKey(it)!!
                }
            )
        }
        throw Error("Key $keyReference not found")
    }

    @TargetApi(23)
    override fun save(keyReference: String, key: SecretKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        val keyValue = Base64.decode(key.k, Base64.URL_SAFE)
        val secret = SecretKeySpec(keyValue, "AES")
        val entry = KeyStore.SecretKeyEntry(secret)
        keyStore.setEntry(alias, entry, secretKeyToKeyProtection(key))
    }

    @TargetApi(23)
    override fun save(keyReference: String, key: PrivateKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        if (keyStore.containsAlias(alias)) {
            // do nothing, the key is already there.
            return
        }
        // This key is not natively supported
        var jwk = key.toJWK();
        println(alias);
        jwk.kid = alias;
        println(jwk);
        val jwkString = MinimalJson.serializer.stringify(JsonWebKey.serializer(), jwk)
        val keyValue = stringToByteArray(jwkString)
        saveSecureData(alias, keyValue)
    }

    override fun save(keyReference: String, key: PublicKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        if (keyStore.containsAlias(alias)) {
            // do nothing, the key is already there.
            return
        }
        throw Error("Why are you even saving a public key; this makes no sense. Rethink your life.")
    }

    override fun list(): Map<String, KeyStoreListItem> {
        val nativeList = listNativeKeys()
        val softwareList = listSecureData()
        return com.microsoft.did.sdk.utilities.Map.join(softwareList, nativeList)
    }


    private fun listNativeKeys(): Map<String, KeyStoreListItem> {
        val output = emptyMap<String, KeyStoreListItem>().toMutableMap()
        val aliases = keyStore.aliases()
        // KeyRef (as key reference) -> KeyRef.VersionNumber (as key identifier)
        val keyContainerPattern = Regex("(^.+).\\d+$")
        for (alias in aliases) {
            if (alias.matches(keyContainerPattern)) {
                val entry = keyStore.getEntry(alias, null)
                val matches = keyContainerPattern.matchEntire(alias)
                val values = matches!!.groupValues

                // Get the keyType associated with this key.
                val kty: KeyType = if (entry is KeyStore.PrivateKeyEntry) {
                    AndroidKeyConverter.whatKeyTypeIs(entry.certificate.publicKey)
                } else { // SecretKeyEntry
                    KeyType.Octets
                }

                // Add the key to an ListItem or make a new one
                if (output.containsKey(values[1])) {
                    val listItem = output[values[1]]!!
                    if (listItem.kty != kty) {
                        throw Error("Key Container ${values[1]} contains keys of two different " +
                                "types (${listItem.kty.value}, ${kty.value})")
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
        val sharedPreferences = getSharedPreferences();
        val keys = sharedPreferences.all.keys;
        // all stored keys should be in JWT format
        val keyMap = mutableMapOf<String, KeyStoreListItem>()
        keys.forEach{
            // verify that it matches the regex and grab the key reference
            val keyReferenceMatch = AndroidKeyStore.regexForKeyReference.matchEntire(it)
            if (keyReferenceMatch != null) {
                val keyRef = keyReferenceMatch.groupValues[1];
                val jwkBase64 = sharedPreferences.getString(it, null)!!
                val jwkData = Base64.decode(jwkBase64, Base64.URL_SAFE)
                val key = MinimalJson.serializer.parse(JsonWebKey.serializer(), byteArrayToString(jwkData))
                val keyType = toKeyType(key.kty)
                if (!keyMap.containsKey(keyRef)) {
                    keyMap[keyRef] = KeyStoreListItem(keyType, mutableListOf(it))
                } else {
                    val listItem = keyMap[keyRef]!!
                    if (keyType != listItem.kty) {
                        throw Error("Key $keyRef has two different key types (${keyType.value}, ${listItem.kty.value})")
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
        val jwk = MinimalJson.serializer.parse(JsonWebKey.serializer(), byteArrayToString(data))
        if (jwk.kty == KeyType.RSA.value) {
            return RsaPrivateKey(jwk)
        } else if (jwk.kty == KeyType.EllipticCurve.value) {
            return EllipticCurvePrivateKey(jwk)
        } else {
            throw Error("Unknown key type ${jwk.kty}")
        }
    }

    private fun getSecureData(alias: String): ByteArray? {
        val sharedPreferences = getSharedPreferences();
        val base64UrlEncodedData = sharedPreferences.getString(alias, null)
        if (base64UrlEncodedData != null) {
            return Base64.decode(base64UrlEncodedData, Base64.URL_SAFE)
        }
        return null
    }

    private fun saveSecureData(alias: String, data: ByteArray) {
        val sharedPreferences = getSharedPreferences();
        val editor = sharedPreferences.edit();
        editor.putString(alias, Base64.encodeToString(data, Base64.URL_SAFE));
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

    @TargetApi(23)
    private fun getSecretVaultMasterKey(): String {
        val alias = "ms-useragent-secret-masterkey"

        if (!keyStore.containsAlias(alias)) {
            // Generate the master key
            val generator = KeyGenerator.getInstance("AES", provider)
            generator.init(KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build())
            generator.generateKey();
        }

        return alias
    }

    @TargetApi(23)
    private fun secretKeyToKeyProtection(key: SecretKey): KeyProtection {
        return keyToKeyProtection(key.key_ops, key.use)
    }

    @TargetApi(23)
    private fun keyToKeyProtection(keyOps: List<KeyUsage>?, keyUse: KeyUse?): KeyProtection {
        val keyUsage = keyOps ?: if (keyUse != null) {
            when (keyUse) {
                KeyUse.Encryption -> listOf(KeyUsage.Encrypt, KeyUsage.Decrypt)
                KeyUse.Signature -> listOf(KeyUsage.Sign, KeyUsage.Verify)
                else -> throw Error("Key use should be either 'sig' or 'enc'")
            }
        } else {
            listOf(KeyUsage.Encrypt, KeyUsage.Decrypt, KeyUsage.Sign, KeyUsage.Verify)
        }
        return keyUsageToKeyProtection(keyUsage)
    }

    @TargetApi(23)
    private fun keyUsageToKeyProtection(usage: List<KeyUsage>): KeyProtection {
        var usageFlag = 0
        if (usage.contains(KeyUsage.Decrypt)) {
            usageFlag = usageFlag or KeyProperties.PURPOSE_DECRYPT
        }
        if (usage.contains(KeyUsage.Encrypt)) {
            usageFlag = usageFlag or KeyProperties.PURPOSE_ENCRYPT
        }
        if (usage.contains(KeyUsage.Sign)) {
            usageFlag = usageFlag or KeyProperties.PURPOSE_SIGN
        }
        if (usage.contains(KeyUsage.Verify)) {
            usageFlag = usageFlag or KeyProperties.PURPOSE_VERIFY
        }
        return KeyProtection.Builder(usageFlag).build()
    }

    fun checkOrCreateKeyId(keyReference: String, kid: String?): String {
        if (!kid.isNullOrBlank() && !kid.startsWith(keyReference)) {
            throw Error("Key ID must begin with key reference")
            // This could be relaxed later if we flush keys and use a format of
            // KEYREFERENCE.KEYID and ensure KEYID does not contain the dot delimiter
        }
        return if (!kid.isNullOrBlank()) {
            kid
        } else {
            // generate a key id
            val listItem = this.list()[keyReference]
            if (listItem == null) { // no previous keys
                "$keyReference.1"
            } else {
                // heuristic, find the last digit and count up
                var latestVersion = listItem.kids.reduce {
                        acc: String, current: String ->
                    val currentValue = regexForKeyIndex.matchEntire(current)?.groupValues?.get(1)?.toInt()
                    val accValue = acc.toIntOrNull()
                    if (currentValue != null && accValue == null) {
                        current
                    } else if (currentValue != null && accValue != null && currentValue > accValue) {
                        current
                    } else {
                        acc
                    }
                }.toIntOrNull() ?: 1

                latestVersion++
                "$keyReference.$latestVersion"
            }
        }
    }


}