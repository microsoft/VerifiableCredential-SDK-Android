package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Subtle
import com.microsoft.did.sdk.utilities.ConsoleLogger
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PublicKeyTest {
    private val logger = ConsoleLogger()
    private val keyStore = InMemoryKeyStore(logger)
    private val subtle = Subtle(setOf(MockProvider()), logger)
    private val crypto = CryptoOperations(keyStore = keyStore, subtleCrypto = subtle, logger = logger)
    lateinit var rsaPublicKey: RsaPublicKey
    @MockK
    lateinit var actualJwk: JsonWebKey

    @BeforeAll
    fun setup() {
        actualJwk = JsonWebKey(
            kty = KeyType.RSA.value,
            alg = "RS256",
            use = KeyUse.Signature.value,
            kid = "#key1",
            key_ops = listOf(KeyUsage.Verify.value)
        )
        rsaPublicKey = RsaPublicKey(actualJwk, logger)

    }

    @Test
    fun rsaPublicKeyInstanceCreationSuccessfulTest() {
        assertThat(rsaPublicKey.kty).isEqualTo(KeyType.RSA)
        assertThat(rsaPublicKey.use).isEqualTo(KeyUse.Signature)
        assertThat(rsaPublicKey.alg).isEqualTo("RS256")
        assertThat(rsaPublicKey.kid).isEqualTo("#key1")
        assertThat(rsaPublicKey.key_ops).containsOnly(KeyUsage.Verify)
    }

    @Test
    fun rsaPublicKeytoJsonWebKeySuccessfulTest() {
        var expectedJwk = rsaPublicKey.toJWK()
        assertThat(actualJwk).isEqualToComparingFieldByFieldRecursively(expectedJwk)
    }

    @Test
    fun rsaPublicKeyInstanceCreationIncorrectKeyTypeTest() {
        actualJwk = mockk(relaxed = true)
        Assertions.assertThatThrownBy { RsaPublicKey(actualJwk, logger) }.isInstanceOf(Error::class.java)
            .hasMessageContaining("Unknown Key Type value: ${actualJwk.kty}")
    }
}