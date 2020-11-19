package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.RsaOaepParams
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.controlflow.UnSupportedAlgorithmException
import com.microsoft.did.sdk.util.defaultTestSerializer
import com.microsoft.did.sdk.util.stringToByteArray
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.random.Random

class JwsTokenTest {
    private val keyStore: InMemoryKeyStore = InMemoryKeyStore()
    private val subtle: SubtleCrypto
    private val crypto: CryptoOperations
    private val keyRef: String
    private val payload = """{"iss":"joe",${'\r'}
 "exp":1300819380,${'\r'}
 "http://example.com/is_root":true}"""

    private val ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()

    init {
        /* This is the payload used for all the operations below */
        subtle = Subtle(setOf(MockProvider()), defaultTestSerializer)
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
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData, defaultTestSerializer)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(defaultTestSerializer, JwsFormat.FlatJson)
        assertThat(serialized).doesNotContain("signatures")
        val verifyToken = JwsToken.deserialize(serialized, defaultTestSerializer)
        assertThat(verifyToken.signatures.size).isEqualTo(1)
    }

    @Test
    fun `test serialization of json in general json format`() {
        val testData: ByteArray = stringToByteArray(payload)
        val token = JwsToken(testData, defaultTestSerializer)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(defaultTestSerializer, JwsFormat.GeneralJson)
        assertThat(serialized).contains("signatures")
        val verifyToken = JwsToken.deserialize(serialized, defaultTestSerializer)
        assertThat(verifyToken.signatures.size).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun signAndVerify() {
        val testData = Random.Default.nextBytes(32)
        val token = JwsToken(testData, defaultTestSerializer)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(defaultTestSerializer, JwsFormat.Compact)
        val verifyToken = JwsToken.deserialize(serialized, defaultTestSerializer)
        Assertions.assertThatThrownBy {
            verifyToken.verify(crypto)
        }.isInstanceOf(
            UnSupportedAlgorithmException::class.java
        )
    }
}