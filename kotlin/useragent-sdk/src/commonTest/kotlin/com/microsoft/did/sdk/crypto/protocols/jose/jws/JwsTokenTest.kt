package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.keys.MockPublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.Base64Url
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class JwsTokenTest {
    val keyStore = InMemoryKeyStore()
    val subtle = Subtle(setOf(MockProvider()))
    val crypto = CryptoOperations(keyStore = keyStore, subtleCrypto = subtle)
    var keyRef = ""

    @BeforeTest
    fun setup() {
        keyRef = Base64Url.encode(Random.nextBytes(8))
        var keyPair =  subtle.generateKeyPair(
            RsaOaepParams(),
            true,
            listOf(KeyUsage.Sign, KeyUsage.Verify)
        )
        val privateKey = MockPrivateKey(subtle.exportKeyJwk(keyPair.privateKey))
        keyStore.save(keyRef, privateKey)
    }

    @ImplicitReflectionSerializer
    @Test
    fun signAndVerify() {
        val testData = Random.Default.nextBytes(32)

        val token = JwsToken(testData)
        token.sign(keyRef, crypto)
        val serialized = token.serialize(JwsFormat.Compact)

        println(serialized)

        val verifyToken = JwsToken.deserialize(serialized)
        verifyToken.verify(crypto)
    }


}