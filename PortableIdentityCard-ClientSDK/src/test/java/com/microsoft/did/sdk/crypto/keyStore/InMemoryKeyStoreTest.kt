package com.microsoft.did.sdk.crypto.keyStore

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.CryptoKeyPair
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.RsaOaepParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.ConsoleLogger
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.random.Random

class InMemoryKeyStoreTest {
    private val logger = ConsoleLogger()
    private val inMemoryKeyStore = InMemoryKeyStore(logger)
    private val subtle = Subtle(setOf(MockProvider()), logger)
    private lateinit var privateKeyRef: String
    private lateinit var publicKeyRef: String
    private lateinit var keyPair: CryptoKeyPair
    private lateinit var actualPublicKey: MockPublicKey
    private lateinit var actualPrivateKey: MockPrivateKey
    private lateinit var secondKeyPair: CryptoKeyPair
    private lateinit var secondPublicKey: MockPublicKey
    private lateinit var secondPrivateKey: MockPrivateKey

    @BeforeAll
    fun setUp() {
        keyPair = subtle.generateKeyPair(
                RsaOaepParams(),
        true,
        listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        actualPrivateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        actualPublicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))
        secondKeyPair = subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        secondPrivateKey = MockPrivateKey(subtle.exportKeyJwk(secondKeyPair.privateKey))
        secondPublicKey = MockPublicKey(subtle.exportKeyJwk(secondKeyPair.publicKey))
    }

    @Test
    fun savePrivateKeyTest() {
        privateKeyRef = Base64Url.encode(Random.nextBytes(8), logger)
        inMemoryKeyStore.save(privateKeyRef, actualPrivateKey)
        val expectedPrivateKeyById = inMemoryKeyStore.getPrivateKeyById(actualPrivateKey.kid)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyById)
        val expectedPrivateKeyByRef = inMemoryKeyStore.getPrivateKey(privateKeyRef)
        assertThat(actualPrivateKey).isEqualTo(expectedPrivateKeyByRef.getKey(expectedPrivateKeyById?.kid))
    }

    @Test
    fun savePublicKeyTest() {
        publicKeyRef = Base64Url.encode(Random.nextBytes(8), logger)
        inMemoryKeyStore.save(publicKeyRef, actualPublicKey)
        val expectedPublicKeyById = inMemoryKeyStore.getPublicKeyById(actualPublicKey.kid)
        assertThat(actualPublicKey).isEqualTo(expectedPublicKeyById)
        val expectedPublicByKeyRef = inMemoryKeyStore.getPublicKey(publicKeyRef)
        assertThat(actualPublicKey).isEqualTo(expectedPublicByKeyRef.getKey(expectedPublicKeyById?.kid))
    }

    @Test
    fun listKeysTest() {
        val keysInKeyStore = inMemoryKeyStore.list()
        val expectedPrivateKey = keysInKeyStore[privateKeyRef]
        assertThat(expectedPrivateKey!!.getLatestKeyId()).isNotNull()
        assertThat(actualPrivateKey.kid).isEqualTo(expectedPrivateKey.kids.firstOrNull())
        val expectedPublicKey = keysInKeyStore[publicKeyRef]
        assertThat(actualPublicKey.kid).isEqualTo(expectedPublicKey!!.kids.firstOrNull())
    }

    @Test
    fun saveSecondPublicKeyTest() {
        inMemoryKeyStore.save(publicKeyRef, secondPublicKey)
        val expectedPublicKeyById = inMemoryKeyStore.getPublicKeyById(secondPublicKey.kid)
        assertThat(secondPublicKey).isEqualTo(expectedPublicKeyById)
        val expectedPublicByKeyRef = inMemoryKeyStore.getPublicKey(publicKeyRef)
        assertThat(secondPublicKey).isEqualTo(expectedPublicByKeyRef.getKey(expectedPublicKeyById?.kid))
    }

    @Test
    fun saveSecondPrivateKeyTest() {
        inMemoryKeyStore.save(privateKeyRef, secondPrivateKey)
        val expectedPrivateKeyById = inMemoryKeyStore.getPrivateKeyById(secondPrivateKey.kid)
        assertThat(secondPrivateKey).isEqualTo(expectedPrivateKeyById)
        val expectedPrivateKeyByRef = inMemoryKeyStore.getPrivateKey(privateKeyRef)
        assertThat(secondPrivateKey).isEqualTo(expectedPrivateKeyByRef.getKey(expectedPrivateKeyById?.kid))
    }

    @Test
    fun getPublicKeyFailureTest() {
        val expectedPublicKeyRef = "kid1"
        assertThatThrownBy {inMemoryKeyStore.getPublicKey(expectedPublicKeyRef)}.isInstanceOf(Error::class.java)
            .hasMessageContaining("key $expectedPublicKeyRef does not exist.")
    }

    @Test
    fun getPublicKeyWithPrivateKeyReferenceTest() {
        val expectedPublicKey = inMemoryKeyStore.getPublicKey(privateKeyRef)
        assertThat(actualPrivateKey.key).isEqualTo(expectedPublicKey.keys[0].key)
    }

    //TO DO: Add tests for secret key
}