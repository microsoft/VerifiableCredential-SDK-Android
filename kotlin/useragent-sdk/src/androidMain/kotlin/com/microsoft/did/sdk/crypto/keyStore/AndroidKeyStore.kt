package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey

class AndroidKeyStore: IKeyStore {
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}