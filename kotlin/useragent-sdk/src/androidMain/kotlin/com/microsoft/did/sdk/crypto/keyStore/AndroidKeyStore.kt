package com.microsoft.did.sdk.crypto.keyStore

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

    override fun getSecretKey(keyReference: String): SecretKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPrivateKey(keyReference: String): PrivateKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPublicKey(keyReference: String): PublicKey {
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

    override fun list(): Map<String, String> {
        val output = emptyMap<String, String>().toMutableMap()
        val aliases = keyStore.aliases()
        for (alias in aliases) {
            if (keyStore.isKeyEntry(alias)) {
                val key = keyStore.getEntry(alias, null)
                if (key is KeyStore.PrivateKeyEntry) {
                    key.certificate.type.
                }
            }
        }
        return output
    }
}