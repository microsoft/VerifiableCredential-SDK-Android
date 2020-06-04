package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.utilities.controlflow.AlgorithmException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class CryptoHelpersTest {

    @Test
    fun `converting json web algorithm to web crypto algorithm for es256k`() {
        val suppliedAlgorithmName = "ES256K"
        val expectedAlgorithmName = "ECDSA"
        val actualAlgorithmName = CryptoHelpers.jwaToWebCrypto(suppliedAlgorithmName).name
        assertThat(actualAlgorithmName).isEqualTo(expectedAlgorithmName)
    }

    @Test
    fun `converting json web algorithm to web crypto algorithm for rs256`() {
        val suppliedAlgorithmName = "RS256"
        val expectedAlgorithmName = "RSASSA-PKCS1-v1_5"
        val actualAlgorithmName = CryptoHelpers.jwaToWebCrypto(suppliedAlgorithmName).name
        assertThat(actualAlgorithmName).isEqualTo(expectedAlgorithmName)
    }

    @Test
    fun `converting json web algorithm to web crypto algorithm for rsaoaep`() {
        val suppliedAlgorithmName = "RSA-OAEP"
        val expectedAlgorithmName = "RSA-OAEP-256"
        val actualAlgorithmName = CryptoHelpers.jwaToWebCrypto(suppliedAlgorithmName).name
        assertThat(actualAlgorithmName).isEqualTo(expectedAlgorithmName)
    }

    @Test
    fun `failing json web algorithm to web crypto algorithm conversion with invalid algorithm name`() {
        val actualAlgorithmName = "SHA-888"
        assertThatThrownBy {
            CryptoHelpers.jwaToWebCrypto(actualAlgorithmName).name
        }.isInstanceOf(AlgorithmException::class.java)
    }

    //TODO: Check for AesGcm128 or AesGcm192 or AesGcm256
}