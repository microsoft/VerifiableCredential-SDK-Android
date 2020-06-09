package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.IKeyStoreItem
import com.microsoft.did.sdk.crypto.keys.KeyContainer
import com.microsoft.did.sdk.crypto.keys.PrivateKey
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.protocols.jose.JwaCryptoConverter
import com.microsoft.did.sdk.util.controlflow.KeyStoreException

class InMemoryKeyStore : KeyStore() {
    private val secretKeys: MutableMap<String, KeyContainer<SecretKey>> = mutableMapOf()
    private val privateKeys: MutableMap<String, KeyContainer<PrivateKey>> = mutableMapOf()
    private val publicKeys: MutableMap<String, KeyContainer<PublicKey>> = mutableMapOf()

    override fun getSecretKey(keyReference: String): KeyContainer<SecretKey> {
        return secretKeys[keyReference] ?: throw KeyStoreException("key $keyReference does not exist.")
    }

    override fun getPrivateKey(keyReference: String): KeyContainer<PrivateKey> {
        return privateKeys[keyReference] ?: throw KeyStoreException("key $keyReference does not exist.")
    }

    override fun getPublicKey(keyReference: String): KeyContainer<PublicKey> {
        return if (publicKeys.containsKey(keyReference)) {
            publicKeys[keyReference]!!
        } else {
            val keyContainer = privateKeys[keyReference] ?: throw KeyStoreException("key $keyReference does not exist.")
            KeyContainer(
                keyContainer.kty,
                keyContainer.keys.map { it.getPublicKey() },
                keyContainer.use,
                keyContainer.alg
            )
        }
    }

    override fun getSecretKeyById(keyId: String): SecretKey? {
        return findKeyMatchingIdIn(secretKeys, keyId)
    }

    override fun getPrivateKeyById(keyId: String): PrivateKey? {
        return findKeyMatchingIdIn(privateKeys, keyId)
    }

    override fun getPublicKeyById(keyId: String): PublicKey? {
        return findKeyMatchingIdIn(publicKeys, keyId) ?: findKeyMatchingIdIn(privateKeys, keyId)?.getPublicKey()
    }

    private fun <T : IKeyStoreItem> findKeyMatchingIdIn(map: Map<String, KeyContainer<T>>, keyId: String): T? {
        return map.map {
            it.value.keys.firstOrNull { key -> key.kid == keyId }
        }.firstOrNull {
            it != null
        }
    }

    override fun save(keyReference: String, key: SecretKey) {
        if (secretKeys.containsKey(keyReference)) {
            val keyContainer = secretKeys[keyReference]!!
            val keys = keyContainer.keys.toMutableList()
            keys.add(key)
            secretKeys[keyReference] = KeyContainer(
                keyContainer.kty,
                keys,
                keyContainer.use,
                keyContainer.alg
            )
        } else {
            secretKeys[keyReference] = KeyContainer(
                key.kty,
                listOf(key),
                KeyUse.Secret
            )
        }
    }

    override fun save(keyReference: String, key: PrivateKey) {
        if (privateKeys.containsKey(keyReference)) {
            val keyContainer = privateKeys[keyReference]!!
            val keys = keyContainer.keys.toMutableList()
            keys.add(key)
            privateKeys[keyReference] = KeyContainer(
                keyContainer.kty,
                keys,
                keyContainer.use,
                keyContainer.alg
            )
        } else {
            privateKeys[keyReference] = KeyContainer(
                key.kty,
                listOf(key),
                key.use,
                key.alg?.let { JwaCryptoConverter.jwaAlgToWebCrypto(it) }
            )
        }
    }

    override fun save(keyReference: String, key: PublicKey) {
        if (publicKeys.containsKey(keyReference)) {
            val keyContainer = publicKeys[keyReference]!!
            val keys = keyContainer.keys.toMutableList()
            keys.add(key)
            publicKeys[keyReference] = KeyContainer(
                keyContainer.kty,
                keys,
                keyContainer.use,
                keyContainer.alg
            )
        } else {
            publicKeys[keyReference] = KeyContainer(
                key.kty,
                listOf(key),
                key.use,
                key.alg?.let { JwaCryptoConverter.jwaAlgToWebCrypto(it) }
            )
        }
    }

    override fun list(): Map<String, KeyStoreListItem> {
        val result = mutableMapOf<String, KeyStoreListItem>()
        secretKeys.forEach {
            result[it.key] = KeyStoreListItem(
                it.value.kty,
                it.value.keys.map { key -> key.kid }.toMutableList()
            )
        }
        privateKeys.forEach {
            result[it.key] = KeyStoreListItem(
                it.value.kty,
                it.value.keys.map { key -> key.kid }.toMutableList()
            )
        }
        publicKeys.forEach {
            result[it.key] = KeyStoreListItem(
                it.value.kty,
                it.value.keys.map { key -> key.kid }.toMutableList()
            )
        }

        return result
    }
}