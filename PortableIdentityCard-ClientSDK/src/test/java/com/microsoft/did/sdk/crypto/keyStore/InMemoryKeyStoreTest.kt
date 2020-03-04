package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.RsaOaepParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.ConsoleLogger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*

class InMemoryKeyStoreTest {
    private val logger = ConsoleLogger()
    private val inMemoryKeyStore = InMemoryKeyStore(logger)
    private val subtle = Subtle(setOf(MockProvider()), logger)
    private val keyRef: String = "TestKeys"
    private var keyPair: CryptoKeyPair
    private var actualPublicKey: MockPublicKey
    private var actualPrivateKey: MockPrivateKey

    init {
        keyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPrivateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        actualPublicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))
    }

    @Test
    fun `save and retrieve public-private keys`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        val expectedPrivateKeyById = inMemoryKeyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyById)
        val expectedPrivateKeyByRef = inMemoryKeyStore.getPrivateKey(keyRef)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyByRef.keys[0])

        inMemoryKeyStore.save(keyRef, actualPublicKey)
        val expectedPublicKeyById = inMemoryKeyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualTo(expectedPublicKeyById)
        val expectedPublicByKeyRef = inMemoryKeyStore.getPublicKey(keyRef)
        assertThat(actualPublicKey).isEqualTo(expectedPublicByKeyRef.keys[0])
    }

    @Test
    fun `list keys`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        val keysInKeyStore = inMemoryKeyStore.list()
        val expectedPrivateKey = keysInKeyStore[keyRef]
        assertThat(expectedPrivateKey!!.getLatestKeyId()).isNotNull()
        assertThat(actualPrivateKey.kid).isEqualTo(expectedPrivateKey.kids.firstOrNull())
        val expectedPublicKey = keysInKeyStore[keyRef]
        assertThat(actualPublicKey.kid).isEqualTo(expectedPublicKey!!.kids.firstOrNull())
    }

    @Test
    fun `save two sets of public-private keys with same key reference and retrieve them`() {
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        inMemoryKeyStore.save(keyRef, actualPrivateKey)

        keyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPrivateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        actualPublicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))

        inMemoryKeyStore.save(keyRef, actualPublicKey)
        inMemoryKeyStore.save(keyRef, actualPrivateKey)

        val expectedPublicKeyById = inMemoryKeyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualTo(expectedPublicKeyById)
        val expectedPublicKeyByRef = inMemoryKeyStore.getPublicKey(keyRef)
        assertThat(expectedPublicKeyByRef.keys).contains(actualPublicKey)

        val expectedPrivateKeyById = inMemoryKeyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyById)
        val expectedPrivateKeyByRef = inMemoryKeyStore.getPrivateKey(keyRef)
        assertThat(expectedPrivateKeyByRef.keys).contains(actualPrivateKey)
    }

    @Test
    fun `fail retrieve public key`() {
        val nonExistingPublicKeyRef = "kid1"
        assertThatThrownBy { inMemoryKeyStore.getPublicKey(nonExistingPublicKeyRef) }.isInstanceOf(Error::class.java)
    }

    @Test
    fun `retrieve public key using key reference`() {
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        val expectedPublicKey = inMemoryKeyStore.getPublicKey(keyRef)
        assertThat(actualPublicKey.key).isEqualTo(expectedPublicKey.keys[0].key)
    }

    @Test
    fun `retrieve public key using key id`() {
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        val expectedPublicKey = inMemoryKeyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualTo(expectedPublicKey)
    }

    @Test
    fun `retrieve private key using key reference`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        val expectedPrivateKey = inMemoryKeyStore.getPrivateKey(keyRef)
        assertThat(actualPrivateKey.key).isEqualTo(expectedPrivateKey.keys[0].key)
    }

    @Test
    fun `retrieve private key using key id`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        val expectedPrivateKey = inMemoryKeyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKey)
    }

    //TODO: Add tests for secret key
}