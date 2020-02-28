package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.utilities.ConsoleLogger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.lang.IllegalStateException
import org.junit.jupiter.api.Test

class CryptoHelpersTest {
    private val logger = ConsoleLogger()

    @Test
    fun es256kJwaToWebCryptoTest() {
        val actualAlgorithmName: String = "ES256K"
        assertThat(CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name).isEqualTo("ECDSA")
    }

    @Test
    fun rs256JwaToWebCryptoTest() {
        val actualAlgorithmName: String = "RS256"
        assertThat(CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name).isEqualTo("RSASSA-PKCS1-v1_5")
    }

    @Test
    fun rsaoaepJwaToWebCryptoTest() {
        val actualAlgorithmName: String = "RSA-OAEP"
        assertThat(CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name).isEqualTo("RSA-OAEP-256")
    }

    @Test
    fun jwaToWebCryptoFailureTest() {
        val actualAlgorithmName: String = "SHA-888"
        assertThatThrownBy { CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Algorithm $actualAlgorithmName is not supported")
    }
}