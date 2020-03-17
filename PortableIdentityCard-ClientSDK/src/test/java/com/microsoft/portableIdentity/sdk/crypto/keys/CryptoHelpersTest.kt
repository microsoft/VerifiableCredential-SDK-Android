package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.utilities.ConsoleLogger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.lang.IllegalStateException
import org.junit.jupiter.api.Test

class CryptoHelpersTest {
    private val logger = ConsoleLogger()

    @Test
    fun `converting json web algorithm to web crypto algorithm for es256k`() {
        val actualAlgorithmName = "ES256K"
        val expectedAlgorithmName = "ECDSA"
        assertThat(CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name).isEqualTo(expectedAlgorithmName)
    }

    @Test
    fun `converting json web algorithm to web crypto algorithm for rs256`() {
        val actualAlgorithmName = "RS256"
        val expectedAlgorithmName = "RSASSA-PKCS1-v1_5"
        assertThat(CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name).isEqualTo(expectedAlgorithmName)
    }

    @Test
    fun `converting json web algorithm to web crypto algorithm for rsaoaep`() {
        val actualAlgorithmName = "RSA-OAEP"
        val expectedAlgorithmName = "RSA-OAEP-256"
        assertThat(CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name).isEqualTo(expectedAlgorithmName)
    }

    @Test
    fun `failing json web algorithm to web crypto algorithm conversion with invalid algorithm name`() {
        val actualAlgorithmName = "SHA-888"
        assertThatThrownBy {
            CryptoHelpers.jwaToWebCrypto(actualAlgorithmName, logger = logger).name
        }.isInstanceOf(IllegalStateException::class.java)
    }
}