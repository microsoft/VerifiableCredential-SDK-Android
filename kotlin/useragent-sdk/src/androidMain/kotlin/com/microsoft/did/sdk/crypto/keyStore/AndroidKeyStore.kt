package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.protocols.jose.JoseConstants
import java.security.KeyStore
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
        val output = emptyMap<String, String>().toMutableMap()
        val aliases = keyStore.aliases()
        // KeyRef (as key reference) -> KeyRef.VersionNumber (as key identifier)
        val keyContainerPattern = Regex("(^.+).(\\d+$)")
        for (alias in aliases) {
            if (alias.matches(keyContainerPattern)) {
                val matches = keyContainerPattern.matchEntire(alias)
                val values = matches!!.groupValues
                if (output.containsKey(values[0])) {

                } else {
                    output[values[0]] = KeyStoreListItem()
                }

            }
        }
        return output
    }
}