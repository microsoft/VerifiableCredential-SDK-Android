package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.*
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import java.security.KeyStore
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey

class AndroidKeyStore: IKeyStore {

    companion object {
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    override fun getSecretKey(keyReference: String): KeyContainer<SecretKey> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPrivateKey(keyReference: String): KeyContainer<PrivateKey> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPublicKey(keyReference: String): KeyContainer<PublicKey> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(keyReference: String, key: SecretKey) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(keyReference: String, key: PrivateKey) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(keyReference: String, key: PublicKey) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    private fun whatKeyTypeIs(publicKey: java.security.PublicKey): KeyType {
        return when (publicKey) {
            is RSAPublicKey -> KeyType.RSA
            is ECPublicKey -> KeyType.EllipticCurve
            else -> throw Error("Unknown Key Type")
        }
    }
}