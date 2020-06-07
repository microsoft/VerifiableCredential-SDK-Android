package com.microsoft.did.sdk.crypto.plugins

import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.serializer.Serializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SubtleCryptoFactoryTest {
    private val serializer = Serializer()
    private val subtle = Subtle(setOf(MockProvider()), serializer)
    private val subtleCryptoFactory = SubtleCryptoFactory(subtle)

    @Test
    fun `get message authentication code signers object for a given algorithm name`() {
        val mockProviderForHmac = MockProvider(W3cCryptoApiConstants.Hmac.value)
        val ecSubtle = Subtle(setOf(mockProviderForHmac), serializer)
        subtleCryptoFactory.addMessageAuthenticationCodeSigner(
            W3cCryptoApiConstants.Hmac.value,
            SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.Private)
        )
        var macSigner = subtleCryptoFactory.getMessageAuthenticationCodeSigners(W3cCryptoApiConstants.Hmac.value, SubtleCryptoScope.Private)
        assertThat((macSigner as Subtle).provider.containsKey(W3cCryptoApiConstants.Hmac.value.toLowerCase())).isTrue()
    }

    @Test
    fun `get message digest for a given algorithm name`() {
        val mockProviderForSha = MockProvider(W3cCryptoApiConstants.Sha512.value)
        val ecSubtle = Subtle(setOf(mockProviderForSha), serializer)
        subtleCryptoFactory.addMessageDigest(W3cCryptoApiConstants.Sha512.value, SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.Public))
        var msgDigest = subtleCryptoFactory.getMessageDigest(W3cCryptoApiConstants.Sha512.value, SubtleCryptoScope.Public)
        assertThat((msgDigest as Subtle).provider.containsKey(W3cCryptoApiConstants.Sha512.value.toLowerCase())).isTrue()
    }

    @Test
    fun `get message signer for a given algorithm name`() {
        val mockProviderForEc = MockProvider(W3cCryptoApiConstants.EcDsa.value)
        val ecSubtle = Subtle(setOf(mockProviderForEc), serializer)
        subtleCryptoFactory.addMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.Private))
        var msgSigner = subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.Private)
        assertThat((msgSigner as Subtle).provider.containsKey(W3cCryptoApiConstants.EcDsa.value.toLowerCase())).isTrue()
    }

    @Test
    fun `get shared key encrypter for a given algorithm`() {
        val mockProviderForRsa = MockProvider(W3cCryptoApiConstants.RsaSsaPkcs1V15.value)
        val rsaSubtle = Subtle(setOf(mockProviderForRsa), serializer)
        subtleCryptoFactory.addSharedKeyEncrypter(
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
            SubtleCryptoMapItem(rsaSubtle, SubtleCryptoScope.Private)
        )
        var sharedKeyEncrypter =
            subtleCryptoFactory.getSharedKeyEncrypter(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, SubtleCryptoScope.Private)
        assertThat((sharedKeyEncrypter as Subtle).provider.containsKey(W3cCryptoApiConstants.RsaSsaPkcs1V15.value.toLowerCase())).isTrue()
    }

    @Test
    fun `get symmetric key encrypter for a given algorithm`() {
        val mockProviderForRsa = MockProvider(W3cCryptoApiConstants.RsaSsaPkcs1V15.value)
        val rsaSubtle = Subtle(setOf(mockProviderForRsa), serializer)
        subtleCryptoFactory.addSymmetricEncrypter(
            W3cCryptoApiConstants.RsaSsaPkcs1V15.value,
            SubtleCryptoMapItem(rsaSubtle, SubtleCryptoScope.Private)
        )
        var symmetricEncrypter =
            subtleCryptoFactory.getSymmetricEncrypter(W3cCryptoApiConstants.RsaSsaPkcs1V15.value, SubtleCryptoScope.Private)
        assertThat((symmetricEncrypter as Subtle).provider.containsKey(W3cCryptoApiConstants.RsaSsaPkcs1V15.value.toLowerCase())).isTrue()
    }
}