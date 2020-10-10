package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.keys.rsa.RsaPublicKey
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.util.controlflow.KeyException
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PublicKeyTest {
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
        Assertions.assertThatThrownBy { RsaPublicKey(actualJwk) }.isInstanceOf(KeyException::class.java)
    }
}