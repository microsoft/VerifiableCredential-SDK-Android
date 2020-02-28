package com.microsoft.did.sdk.crypto.keys

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.utilities.ConsoleLogger
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class KeyTypeFactoryTest {
    private val logger = ConsoleLogger()

    @Test
    fun rsaoaepKeyTypeTest() {
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm("rsa-oaep"))
        assertThat(actualKeyType).isEqualTo(KeyType.RSA)
    }

    @Test
    fun rsaoaep256KeyTypeTest() {
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm("rsa-oaep-256"))
        assertThat(actualKeyType).isEqualTo(KeyType.RSA)
    }

    @Test
    fun hmacKeyTypeTest() {
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm("hmac"))
        assertThat(actualKeyType).isEqualTo(KeyType.Octets)
    }

    @Test
    fun ecdsaKeyTypeTest() {
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm("ecdsa"))
        assertThat(actualKeyType).isEqualTo(KeyType.EllipticCurve)
    }

    @Test
    fun ecdhKeyTypeTest() {
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm("ecdh"))
        assertThat(actualKeyType).isEqualTo(KeyType.EllipticCurve)
    }

    @Test
    fun rsaKeyTypeTest() {
        val actualKeyType = KeyTypeFactory.createViaWebCrypto(Algorithm("rsassa-pkcs1-v1_5"))
        assertThat(actualKeyType).isEqualTo(KeyType.RSA)
    }

    @Test
    fun keyTypeFailureTest() {
        val actualAlgorithmName = "Test"
        assertThatThrownBy { KeyTypeFactory.createViaJwa(actualAlgorithmName, logger) }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Algorithm $actualAlgorithmName is not supported")

    }
}