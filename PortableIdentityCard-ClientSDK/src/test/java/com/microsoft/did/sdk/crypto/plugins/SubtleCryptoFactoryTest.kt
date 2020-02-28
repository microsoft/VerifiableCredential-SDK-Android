package com.microsoft.did.sdk.crypto.plugins

import assertk.assertThat
import assertk.assertions.isNotNull
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.ConsoleLogger
import org.junit.jupiter.api.Test

class SubtleCryptoFactoryTest {
    private val logger = ConsoleLogger()
    private val subtle = Subtle(setOf(MockProvider()), logger)
    private val subtleCryptoFactory = SubtleCryptoFactory(subtle, logger)

    @Test
    fun instanceCreationTest() {
        assertThat(subtleCryptoFactory).isNotNull()
    }

    @Test
    fun getMessageAuthenticationCodeSignersTest() {
        val macSigner = subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private)
        assertThat(macSigner).isNotNull()
    }

    @Test
    fun getMessageDigestTest() {
        val msgDigest = subtleCryptoFactory.getMessageDigest(W3cCryptoApiConstants.Sha512.value, SubtleCryptoScope.Public)
        assertThat(msgDigest).isNotNull()
    }

    @Test
    fun getMessageSignerTest() {
        val msgSigner = subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.Private)
        assertThat(msgSigner).isNotNull()
    }

    @Test
    fun getSharedKeyEncrypterTest() {
        val sharedKeyEncrypter =
            subtleCryptoFactory.getSharedKeyEncrypter(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, SubtleCryptoScope.Private)
        assertThat(sharedKeyEncrypter).isNotNull()
    }
}