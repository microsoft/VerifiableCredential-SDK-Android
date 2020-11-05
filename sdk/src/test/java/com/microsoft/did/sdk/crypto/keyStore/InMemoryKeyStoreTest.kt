package com.microsoft.did.sdk.crypto.keyStore

import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaOaepParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.util.controlflow.KeyStoreException
import com.microsoft.did.sdk.util.defaultTestSerializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class InMemoryKeyStoreTest {
    private val inMemoryKeyStore = InMemoryKeyStore()
    private val subtle = Subtle(setOf(MockProvider()), defaultTestSerializer)
    private val keyRef: String = "TestKeys"
    private var keyPair: CryptoKeyPair
    private var actualPublicKey: MockPublicKey
    private var actualPrivateKey: MockPrivateKey
    private var actualSecretKey: SecretKey

    init {
        keyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPrivateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        actualPublicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))
        actualSecretKey = SecretKey(JsonWebKey(k = "TestSecretKey1"))
    }

    @Test
    fun `save and retrieve private key`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        val expectedPrivateKeyById = inMemoryKeyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyById)
        val expectedPrivateKeyByRef = inMemoryKeyStore.getPrivateKey(keyRef)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyByRef.keys[0])
    }

    @Test
    fun `save and retrieve public key`() {
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        val expectedPublicKeyById = inMemoryKeyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualTo(expectedPublicKeyById)
        val expectedPublicByKeyRef = inMemoryKeyStore.getPublicKey(keyRef)
        assertThat(actualPublicKey).isEqualTo(expectedPublicByKeyRef.keys[0])
    }

    @Test
    fun `save and retrieve secret key`() {
        inMemoryKeyStore.save(keyRef, actualSecretKey)
        val expectedSecretKeyById = inMemoryKeyStore.getSecretKeyById(actualSecretKey.kid)
        assertThat(actualSecretKey).isEqualTo(expectedSecretKeyById)
        val expectedSecretByKeyRef = inMemoryKeyStore.getSecretKey(keyRef)
        assertThat(actualSecretKey).isEqualTo(expectedSecretByKeyRef.keys[0])
    }

    @Test
    fun `list keys`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        keyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPrivateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        actualPublicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))
        inMemoryKeyStore.save(keyRef, actualPrivateKey)
        inMemoryKeyStore.save(keyRef, actualPublicKey)
        val keysInKeyStore = inMemoryKeyStore.list()
        val expectedPrivateKey = keysInKeyStore[keyRef]
        assertThat(expectedPrivateKey?.kids?.size).isEqualTo(2)
    }

    @Test
    fun `save two sets of public keys with same key reference and retrieve them`() {
        inMemoryKeyStore.save(keyRef, actualPublicKey)

        keyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPublicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))

        inMemoryKeyStore.save(keyRef, actualPublicKey)

        val expectedPublicKeyById = inMemoryKeyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualTo(expectedPublicKeyById)
        val expectedPublicKeyByRef = inMemoryKeyStore.getPublicKey(keyRef)
        assertThat(expectedPublicKeyByRef.keys).contains(actualPublicKey)
    }

    @Test
    fun `save two sets of private keys with same key reference and retrieve them`() {
        inMemoryKeyStore.save(keyRef, actualPrivateKey)

        keyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPrivateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))

        inMemoryKeyStore.save(keyRef, actualPrivateKey)

        val expectedPrivateKeyById = inMemoryKeyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyById)
        val expectedPrivateKeyByRef = inMemoryKeyStore.getPrivateKey(keyRef)
        assertThat(expectedPrivateKeyByRef.keys).contains(actualPrivateKey)
    }

    @Test
    fun `save two sets of secret keys with same key reference and retrieve them`() {
        inMemoryKeyStore.save(keyRef, actualSecretKey)
        actualSecretKey = SecretKey(JsonWebKey(k = "TestSecretKeys2"))
        inMemoryKeyStore.save(keyRef, actualSecretKey)
        val expectedSecretKeyByRef = inMemoryKeyStore.getSecretKey(keyRef)
        assertThat(expectedSecretKeyByRef.keys).contains(actualSecretKey)
    }

    @Test
    fun `fail retrieve public key`() {
        val nonExistingPublicKeyRef = "kid1"
        assertThatThrownBy { inMemoryKeyStore.getPublicKey(nonExistingPublicKeyRef) }.isInstanceOf(KeyStoreException::class.java)
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

    @Test
    fun `retrieve secret key using key reference`() {
        inMemoryKeyStore.save(keyRef, actualSecretKey)
        val expectedSecretKey = inMemoryKeyStore.getSecretKey(keyRef)
        assertThat(actualSecretKey.k).isEqualTo(expectedSecretKey.keys[0].k)
    }

    @Test
    fun `retrieve secret key using key id`() {
        inMemoryKeyStore.save(keyRef, actualSecretKey)
        val expectedSecretKey = inMemoryKeyStore.getSecretKeyById(actualSecretKey.kid)
        assertThat(actualSecretKey).isEqualTo(expectedSecretKey)
    }
}