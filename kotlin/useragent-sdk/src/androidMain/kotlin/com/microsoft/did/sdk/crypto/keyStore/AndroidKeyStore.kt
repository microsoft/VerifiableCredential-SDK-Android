package com.microsoft.did.sdk.crypto.keyStore

import android.util.Base64
import com.microsoft.did.sdk.crypto.keys.*
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePublicKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPrivateKey
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import java.security.KeyFactory
import java.security.KeyStore
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec

class AndroidKeyStore: IKeyStore {

    companion object {
        const val provider = "AndroidKeyStore"

        val keyStore: KeyStore = KeyStore.getInstance(provider).apply {
            load(null)
        }

        fun androidPublicKeyToPublicKey(alias: String, publicKey: java.security.PublicKey): PublicKey {
            return when (whatKeyTypeIs(publicKey)) {
                KeyType.RSA -> {
                    RsaPublicKey(
                        JsonWebKey(
                            kty = KeyType.RSA.value,
                            kid = alias,
                            n = Base64.encodeToString((publicKey as RSAPublicKey).modulus.toByteArray(), Base64.URL_SAFE),
                            e = Base64.encodeToString(publicKey.publicExponent.toByteArray(), Base64.URL_SAFE)
                        )
                    )
                }
                KeyType.EllipticCurve -> {
                    EllipticCurvePublicKey(
                        JsonWebKey(
                            kty = KeyType.EllipticCurve.value,
                            kid = alias,
                            x = Base64.encodeToString((publicKey as ECPublicKey).w.affineX.toByteArray(), Base64.URL_SAFE),
                            y = Base64.encodeToString(publicKey.w.affineY.toByteArray(), Base64.URL_SAFE)
                        )
                    )
                }
                else -> throw Error("Cannot convert key type.")
            }
        }

        fun androidPrivateKeyToPrivateKey(alias: String, privateKey: KeyStore.PrivateKeyEntry): PrivateKey {
            val publicKey = whatKeyTypeIs(privateKey.certificate.publicKey)
            return when (publicKey) {
                KeyType.RSA -> {
                    RsaPrivateKey (
                        JsonWebKey(
                            kty = KeyType.RSA.value,
                            kid = alias,
                            n = Base64.encodeToString((publicKey as RSAPublicKey).modulus.toByteArray(), Base64.URL_SAFE),
                            e = Base64.encodeToString(publicKey.publicExponent.toByteArray(), Base64.URL_SAFE),
                            d = "0",
                            p = "0",
                            q = "0",
                            dp = "0",
                            dq = "0",
                            qi = "0"
                        )
                    )
                }
                KeyType.EllipticCurve -> {
                    EllipticCurvePrivateKey (
                        JsonWebKey(
                            kty = KeyType.EllipticCurve.value,
                            kid = alias,
                            x = Base64.encodeToString((publicKey as ECPublicKey).w.affineX.toByteArray(), Base64.URL_SAFE),
                            y = Base64.encodeToString(publicKey.w.affineY.toByteArray(), Base64.URL_SAFE),
                            d = "0"
                        )
                    )
                }
                else -> throw Error("Cannot convert key type.")
            }
        }

        fun androidSecretKeyToSecretKey(alias: String, secretKey: KeyStore.SecretKeyEntry): SecretKey {
            return SecretKey(JsonWebKey(
                kty = KeyType.Octets.value,
                kid = alias,
                k = Base64.encodeToString(secretKey.secretKey.encoded, Base64.URL_SAFE)
            ))
        }

        fun checkOrCreateKeyId(keyReference: String, kid: String?): String {
            if (!kid.isNullOrBlank() && !kid!!.startsWith(keyReference)) {
                throw Error("Key ID must begin with key reference")
                // This could be relaxed later if we flush keys and use a format of
                // KEYREFERENCE.KEYID and ensure KEYID does not contain the dot delimiter
            }
            val regexForIndex = Regex("^.*\\.([-.]+$)")
            return if (kid != null) {
                kid
            } else {
                // generate a key id
                val listItem = list()[keyReference]
                if (listItem == null) { // no previous keys
                    "$keyReference.1"
                } else {
                    // heuristic, find the last digit and count up
                    var latestVersion = listItem.kids.reduce {
                            acc: String, current: String ->
                        val currentValue = regexForIndex.matchEntire(current)?.groupValues?.firstOrNull()?.toInt()
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

        private fun whatKeyTypeIs(publicKey: java.security.PublicKey): KeyType {
            return when (publicKey) {
                is RSAPublicKey -> KeyType.RSA
                is ECPublicKey -> KeyType.EllipticCurve
                else -> throw Error("Unknown Key Type")
            }
        }
    }

    override fun getSecretKey(keyReference: String): KeyContainer<SecretKey> {
        val allKeys = list()
        val key = allKeys[keyReference] ?: throw Error("Key $keyReference not found")
        if (key.kty != KeyType.Octets) {
            throw Error("Key $keyReference (type ${key.kty.value}) is not a secret.")
        }
        return KeyContainer(
            kty = key.kty,
            keys = key.kids.map {
                androidSecretKeyToSecretKey(it,
                    keyStore.getEntry(it, null) as? KeyStore.SecretKeyEntry ?: throw Error("Key $it is not a secret key."))
            }
        )
    }

    override fun getPrivateKey(keyReference: String): KeyContainer<PrivateKey> {
        val allKeys = list()
        val key = allKeys[keyReference] ?: throw Error("Key $keyReference not found")
        return KeyContainer(
            kty = key.kty,
            keys = key.kids.map{
                androidPrivateKeyToPrivateKey(it,
                        keyStore.getEntry(it, null) as? KeyStore.PrivateKeyEntry ?: throw Error("Key $it is not a private key."))
            }
        )
    }

    override fun getPublicKey(keyReference: String): KeyContainer<PublicKey> {
        val allKeys = list()
        val key = allKeys[keyReference] ?: throw Error("Key $keyReference not found")
        return KeyContainer(
            kty = key.kty,
            keys = key.kids.map {
                val entry = keyStore.getEntry(it, null) as? KeyStore.PrivateKeyEntry ?: throw Error("Key $it is not a private key.")
                androidPublicKeyToPublicKey(it, entry.certificate.publicKey)
            }
        )
    }

    override fun save(keyReference: String, key: SecretKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        val keyValue = Base64.decode(key.k, Base64.URL_SAFE)
        keyStore.setKeyEntry(alias, keyValue, emptyArray())
    }

    override fun save(keyReference: String, key: PrivateKey) {
        val alias = checkOrCreateKeyId(keyReference, key.kid)
        if (keyStore.containsAlias(alias)) {
            // do nothing, the key is already there.
            return
        }
        throw Error("Software Keys are currently not supported.")
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
        val output = emptyMap<String, KeyStoreListItem>().toMutableMap()
        val aliases = keyStore.aliases()
        // KeyRef (as key reference) -> KeyRef.VersionNumber (as key identifier)
        val keyContainerPattern = Regex("(^.+).(\\d+$)")
        for (alias in aliases) {
            if (alias.matches(keyContainerPattern)) {
                val entry = keyStore.getEntry(alias, null)
                val matches = keyContainerPattern.matchEntire(alias)
                val values = matches!!.groupValues

                // Get the keyType associated with this key.
                val kty: KeyType = if (entry is KeyStore.PrivateKeyEntry) {
                    whatKeyTypeIs(entry.certificate.publicKey)
                } else { // SecretKeyEntry
                    KeyType.Octets
                }

                // Add the key to an ListItem or make a new one
                if (output.containsKey(values[0])) {
                    val listItem = output[values[0]]!!
                    if (listItem.kty != kty) {
                        throw Error("Key Container ${values[0]} contains keys of two different " +
                                "types (${listItem.kty.value}, ${kty.value})")
                    }
                    listItem.kids.add(alias)
                } else {
                    output[values[0]] = KeyStoreListItem(kty, mutableListOf(alias))
                }
            }
        }
        return output
    }

}