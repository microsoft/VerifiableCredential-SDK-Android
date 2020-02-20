package com.microsoft.did.sdk.crypto.protocols.jose.jws

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.MockPrivateKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.*
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.ConsoleLogger
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test

class JwsTokenTest {
    val logger = ConsoleLogger()
    val keyStore = InMemoryKeyStore(logger)
    val subtle = Subtle(setOf(MockProvider()), logger)
    val crypto = CryptoOperations(keyStore = keyStore, subtleCrypto = subtle, logger = logger)
    var keyRef = ""

    @BeforeTest
    fun setup() {
        keyRef = Base64Url.encode(Random.nextBytes(8), logger)
        var keyPair =  subtle.generateKeyPair(
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
    }


}