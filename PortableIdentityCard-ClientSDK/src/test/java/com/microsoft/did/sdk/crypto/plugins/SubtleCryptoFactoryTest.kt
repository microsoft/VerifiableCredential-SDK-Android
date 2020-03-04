package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.ConsoleLogger
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubtleCryptoFactoryTest {
    private val logger = ConsoleLogger()
    private val subtle = Subtle(setOf(MockProvider()), logger)
    private val subtleCryptoFactory = SubtleCryptoFactory(subtle, logger)

    @Test
    fun `get message authentication code signers object for a given algorithm name`() {
        val mockProviderForHmac = MockProvider()
        mockProviderForHmac.name = W3cCryptoApiConstants.Hmac.value
        val ecSubtle = Subtle(setOf(mockProviderForHmac), logger)
        subtleCryptoFactory.addMessageAuthenticationCodeSigner(
            W3cCryptoApiConstants.Hmac.value,
            SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.Private)
        )
        var macSigner = subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private)
        if (macSigner is Subtle) {
            macSigner = macSigner as Subtle
            assertThat(macSigner.provider.containsKey(W3cCryptoApiConstants.Hmac.value.toLowerCase())).isTrue()
        }
    }

    @Test
    fun `get message digest for a given algorithm name`() {
        val mockProviderForSha = MockProvider()
        mockProviderForSha.name = W3cCryptoApiConstants.Sha512.value
        val ecSubtle = Subtle(setOf(mockProviderForSha), logger)
        subtleCryptoFactory.addMessageDigest(W3cCryptoApiConstants.Sha512.value, SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.Public))
        var msgDigest = subtleCryptoFactory.getMessageDigest(W3cCryptoApiConstants.Sha512.value, SubtleCryptoScope.Public)
        if (msgDigest is Subtle) {
            msgDigest = msgDigest as Subtle
            assertThat(msgDigest.provider.containsKey(W3cCryptoApiConstants.Sha512.value.toLowerCase())).isTrue()
        }
    }

    @Test
    fun `get message signer for a given algorithm name`() {
        val mockProviderForEc = MockProvider()
        mockProviderForEc.name = W3cCryptoApiConstants.EcDsa.value
        val ecSubtle = Subtle(setOf(mockProviderForEc), logger)
        subtleCryptoFactory.addMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.Private))
        var msgSigner = subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.Private)
        if (msgSigner is Subtle) {
            msgSigner = msgSigner as Subtle
            assertThat(msgSigner.provider.containsKey(W3cCryptoApiConstants.EcDsa.value.toLowerCase())).isTrue()
        }
    }

    @Test
    fun `get shared key encrypter for a given algorithm`() {
        val mockProviderForRsa = MockProvider()
        mockProviderForRsa.name = W3cCryptoApiConstants.RsaSsaPkcs1V15.value
        val rsaSubtle = Subtle(setOf(mockProviderForRsa), logger)
        subtleCryptoFactory.addSharedKeyEncrypter(
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
            SubtleCryptoMapItem(rsaSubtle, SubtleCryptoScope.Private)
        )
        var sharedKeyEncrypter =
            subtleCryptoFactory.getSharedKeyEncrypter(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, SubtleCryptoScope.Private)
        if (sharedKeyEncrypter is Subtle) {
            sharedKeyEncrypter = sharedKeyEncrypter as Subtle
            assertThat(sharedKeyEncrypter.provider.containsKey(W3cCryptoApiConstants.RsaSsaPkcs1V15.value.toLowerCase())).isTrue()
        }
    }

    @Test
    fun `get symmetric key encrypter for a given algorithm`() {
        val mockProviderForRsa = MockProvider()
        mockProviderForRsa.name = W3cCryptoApiConstants.RsaSsaPkcs1V15.value
        val rsaSubtle = Subtle(setOf(mockProviderForRsa), logger)
        subtleCryptoFactory.addSymmetricEncrypter(
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
            SubtleCryptoMapItem(rsaSubtle, SubtleCryptoScope.Private)
        )
        var symmetricEncrypter =
            subtleCryptoFactory.getSymmetricEncrypter(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, SubtleCryptoScope.Private)
        if (symmetricEncrypter is Subtle) {
            symmetricEncrypter = symmetricEncrypter as Subtle
            assertThat(symmetricEncrypter.provider.containsKey(W3cCryptoApiConstants.RsaSsaPkcs1V15.value.toLowerCase())).isTrue()
        }
    }
}