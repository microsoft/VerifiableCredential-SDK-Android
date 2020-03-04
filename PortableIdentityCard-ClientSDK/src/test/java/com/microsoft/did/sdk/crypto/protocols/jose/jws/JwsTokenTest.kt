package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.ILogger
import org.junit.jupiter.api.Test
import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import com.microsoft.did.sdk.utilities.*

class JwsTokenTest {
    private val logger: ILogger
    private val keyStore: IKeyStore
    private val subtle: SubtleCrypto
    private val crypto: CryptoOperations
    private val keyRef: String
    private val payload: String = "{\"iss\":\"joe\",\r\n"+
    " \"exp\":1300819380,\r\n"+
            " \"http://example.com/is_root\":true}"

    init {
        /* This is the payload used for all the operations below */
        logger = ConsoleLogger()
        keyStore = InMemoryKeyStore(logger)
        subtle = Subtle(setOf(MockProvider()), logger)
        crypto = CryptoOperations(subtle, keyStore, logger)
        keyRef = Base64Url.encode(Random.nextBytes(8), logger)
        val keyPair =  subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        val privateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        keyStore.save(keyRef, privateKey)
        val publicKey = MockPublicKey(subtle.exportKeyJwk(keyPair.publicKey))
        keyStore.save(keyRef, publicKey)
    }

    @Test
    fun `test serialization of json in flat format`() {
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData, logger)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.FlatJson)
        assertThat(serialized).isNotNull()
        assertThat(serialized).doesNotContain("signatures")
        val verifyToken = JwsToken.deserialize(serialized, logger)
        assertThat(verifyToken).isNotNull
        assertThat(verifyToken.signatures.size).isEqualTo(1)
    }

    @Test
    fun `test serialization of json in general json format`() {
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData, logger)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.GeneralJson)
        assertThat(serialized).isNotNull()
        assertThat(serialized).contains("signatures")
        val verifyToken = JwsToken.deserialize(serialized, logger)
        assertThat(verifyToken).isNotNull
        assertThat(verifyToken.signatures.size).isGreaterThanOrEqualTo(1)
    }


}