package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaOaepParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.Serializer
import kotlin.random.Random
import org.assertj.core.api.Assertions.assertThat
import com.microsoft.did.sdk.utilities.stringToByteArray
import org.junit.Test

class JwsTokenTest {
    private val keyStore: InMemoryKeyStore = InMemoryKeyStore()
    private val subtle: SubtleCrypto
    private val crypto: CryptoOperations
    private val keyRef: String
    private val payload = """{"iss":"joe",${'\r'}
 "exp":1300819380,${'\r'}
 "http://example.com/is_root":true}"""

    private val serializer = Serializer()
    private val ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()

    init {
        /* This is the payload used for all the operations below */
        subtle = Subtle(setOf(MockProvider()), serializer)
        crypto = CryptoOperations(subtle, keyStore, ellipticCurvePairwiseKey)
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
        val serializer = Serializer()
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData, serializer)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(serializer, JwsFormat.FlatJson)
        assertThat(serialized).doesNotContain("signatures")
        val verifyToken = JwsToken.deserialize(serialized, serializer)
        assertThat(verifyToken.signatures.size).isEqualTo(1)
    }

    @Test
    fun `test serialization of json in general json format`() {
        val serializer = Serializer()
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData, serializer)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(serializer, JwsFormat.GeneralJson)
        assertThat(serialized).contains("signatures")
        val verifyToken = JwsToken.deserialize(serialized, serializer)
        assertThat(verifyToken.signatures.size).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun signAndVerify() {
        val serializer = Serializer()
        val testData = Random.Default.nextBytes(32)
        val token = JwsToken(testData, serializer)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(serializer, JwsFormat.Compact)
        val verifyToken = JwsToken.deserialize(serialized, serializer)
        val matched = verifyToken.verify(crypto)
        assertThat(matched).isTrue()
    }
}