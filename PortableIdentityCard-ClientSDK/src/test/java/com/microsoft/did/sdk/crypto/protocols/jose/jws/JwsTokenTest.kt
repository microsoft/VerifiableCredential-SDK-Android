package com.microsoft.did.sdk.crypto.protocols.jose.jws

import assertk.assertThat
import assertk.assertions.*
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.ConsoleLogger
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.random.Random

class JwsTokenTest {
    private val logger = ConsoleLogger()
    private val keyStore = InMemoryKeyStore(logger)
    private val subtle = Subtle(setOf(MockProvider()), logger)
    private val crypto = CryptoOperations(keyStore = keyStore, subtleCrypto = subtle, logger = logger)
    private var keyRef = ""

    @BeforeAll
    fun setup() {
        keyRef = Base64Url.encode(Random.nextBytes(8), logger)
        val keyPair =  subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        val privateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        keyStore.save(keyRef, privateKey)
    }

    @Test
    fun signAndVerify() {
        val testData = Random.Default.nextBytes(32)
        val token = JwsToken(testData, logger)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.Compact)

        println(serialized)

        val verifyToken = JwsToken.deserialize(serialized, logger)
        verifyToken.verify(crypto)
        assertThat(verifyToken.signatures[0].header).isNull()
    }
    
    @Test
    fun serializerFlatFormatTest() {
        val testData: ByteArray = Random.Default.nextBytes(32)
        val token = JwsToken(testData, logger)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.FlatJson)
        assertThat(serialized).isNotNull()
        assertThat(serialized).doesNotContain("signatures")
        val verifyToken = JwsToken.deserialize(serialized, logger)
        assertThat(verifyToken).isNotNull()
        assertThat(verifyToken.signatures.size).isEqualTo(1)
    }

    @Test
    fun serializerGeneralJsonTest() {
        val testData: ByteArray = Random.Default.nextBytes(32)
        val token = JwsToken(testData, logger)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.GeneralJson)
        assertThat(serialized).isNotNull()
        assertThat(serialized).contains("signatures")
        val verifyToken = JwsToken.deserialize(serialized, logger)
        assertThat(verifyToken).isNotNull()
        assertThat(verifyToken.signatures.size).isGreaterThanOrEqualTo(1)
    }


}