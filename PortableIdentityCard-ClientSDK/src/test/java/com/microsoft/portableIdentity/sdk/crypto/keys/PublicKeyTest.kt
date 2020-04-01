package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.keyStore.InMemoryKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.MockProvider
import com.microsoft.portableIdentity.sdk.crypto.plugins.subtleCrypto.Subtle
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

class PublicKeyTest {
    private val keyStore = InMemoryKeyStore()
    private val subtle = Subtle(setOf(MockProvider()))
    private var rsaPublicKey: RsaPublicKey
    @MockK
    private var actualJwk: JsonWebKey

    init {
        actualJwk = JsonWebKey(
            kty = KeyType.RSA.value,
            alg = "RS256",
            use = KeyUse.Signature.value,
            kid = "#key1",
            key_ops = listOf(KeyUsage.Verify.value)
        )
        rsaPublicKey = RsaPublicKey(actualJwk)

    }

    @Test
    fun `rsa public key creation`() {
        assertThat(rsaPublicKey.kty).isEqualTo(KeyType.RSA)
        assertThat(rsaPublicKey.use).isEqualTo(KeyUse.Signature)
        assertThat(rsaPublicKey.alg).isEqualTo("RS256")
        assertThat(rsaPublicKey.kid).isEqualTo("#key1")
        assertThat(rsaPublicKey.key_ops).containsOnly(KeyUsage.Verify)
    }

    @Test
    fun `converting rsa public key to json web key`() {
        val expectedJwk = rsaPublicKey.toJWK()
        assertThat(actualJwk).isEqualToComparingFieldByFieldRecursively(expectedJwk)
    }

    @Test
    fun `failing rsa public key creation with wrong key type`() {
        actualJwk = mockk(relaxed = true)
        Assertions.assertThatThrownBy { RsaPublicKey(actualJwk) }.isInstanceOf(RuntimeException::class.java)
    }
}