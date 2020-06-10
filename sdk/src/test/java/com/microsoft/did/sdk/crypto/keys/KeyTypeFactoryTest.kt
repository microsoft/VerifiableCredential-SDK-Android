package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KeyTypeFactoryTest {

    @Test
    fun `finding key type for rsa-oaep algorithm`() {
        val algorithmName = "rsa-oaep"
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm(algorithmName))
        assertThat(actualKeyType).isEqualTo(KeyType.RSA)
    }

    @Test
    fun `finding key type for rsa-oaep-256 algorithm`() {
        val algorithmName = "rsa-oaep-256"
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm(algorithmName))
        assertThat(actualKeyType).isEqualTo(KeyType.RSA)
    }

    @Test
    fun `finding key type for hmac algorithm`() {
        val algorithmName = "hmac"
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm(algorithmName))
        assertThat(actualKeyType).isEqualTo(KeyType.Octets)
    }

    @Test
    fun `finding key type for ecdsa algorithm`() {
        val algorithmName = "ecdsa"
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm(algorithmName))
        assertThat(actualKeyType).isEqualTo(KeyType.EllipticCurve)
    }

    @Test
    fun `finding key type for ecdh algorithm`() {
        val algorithmName = "ecdh"
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm(algorithmName))
        assertThat(actualKeyType).isEqualTo(KeyType.EllipticCurve)
    }

    @Test
    fun `finding key type for rsa algorithm`() {
        val algorithmName = "rsassa-pkcs1-v1_5"
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm(algorithmName))
        assertThat(actualKeyType).isEqualTo(KeyType.RSA)
    }
}