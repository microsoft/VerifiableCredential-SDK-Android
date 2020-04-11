package com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.MockPrivateKey
import com.microsoft.portableIdentity.sdk.crypto.keys.MockPublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray
import org.junit.Test

class JwsTokenTest {
    private val keyStore: InMemoryKeyStore = InMemoryKeyStore()
    private val subtle: SubtleCrypto
    private val crypto: CryptoOperations
    private val keyRef: String
    private val payload = """{"iss":"joe",${'\r'}
 "exp":1300819380,${'\r'}
 "http://example.com/is_root":true}"""

    init {
        /* This is the payload used for all the operations below */
        subtle = Subtle(setOf(MockProvider()))
        crypto = CryptoOperations(subtle, keyStore)
        keyRef = Base64Url.encode(Random.nextBytes(8))
        val keyPair = subtle.generateKeyPair(
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
        val token = JwsToken(testData)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.FlatJson)
        assertThat(serialized).isNotNull()
        assertThat(serialized).doesNotContain("signatures")
        val verifyToken = JwsToken.deserialize(serialized)
        assertThat(verifyToken).isNotNull()
        assertThat(verifyToken.signatures.size).isEqualTo(1)
    }

    @Test
    fun `test serialization of json in general json format`() {
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.GeneralJson)
        assertThat(serialized).isNotNull()
        assertThat(serialized).contains("signatures")
        val verifyToken = JwsToken.deserialize(serialized)
        assertThat(verifyToken).isNotNull()
        assertThat(verifyToken.signatures.size).isGreaterThanOrEqualTo(1)
    }
}