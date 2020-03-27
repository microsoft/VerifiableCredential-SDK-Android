package com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.MockPrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.*
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
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