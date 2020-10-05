package com.microsoft.did.sdk.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Base64Test {

    /* @see https://tools.ietf.org/html/rfc4648#section-10
    */
    private val base64TestPairs = listOf(
        Pair("", ""),
        Pair("f", "Zg=="),
        Pair("fo", "Zm8="),
        Pair("foo", "Zm9v"),
        Pair("foob", "Zm9vYg=="),
        Pair("fooba", "Zm9vYmE="),
        Pair("foobar", "Zm9vYmFy")
    )

    private val base64UrlTestPairs = listOf(
        Pair("", ""),
        Pair("f", "Zg"),
        Pair("fo", "Zm8"),
        Pair("foo", "Zm9v"),
        Pair("foob", "Zm9vYg"),
        Pair("fooba", "Zm9vYmE"),
        Pair("foobar", "Zm9vYmFy")
    )

    @Test
    fun `rfc base64 encode vectors`() {
        base64TestPairs.forEach {
            val suppliedInput = it.first.map { character -> character.toByte() }.toByteArray()
            val actualEncodedOutput = Base64.encode(suppliedInput)
            val expectedEncodedOutput = it.second
            assertThat(actualEncodedOutput).isEqualTo(expectedEncodedOutput)
        }
    }

    @Test
    fun `rfc base64 decode vectors`() {
        base64TestPairs.forEach {
            val expectedDecodedOutput = it.first.map { character -> character.toByte() }.toByteArray()
            val suppliedInput = it.second
            val actualDecodedOutput = Base64.decode(suppliedInput)
            assertEqualsByteArray(expectedDecodedOutput, actualDecodedOutput)
        }
    }

    @Test
    fun `rfc base64url encode vectors`() {
        base64UrlTestPairs.forEach {
            val suppliedInput = it.first.map { character -> character.toByte() }.toByteArray()
            val actualEncodedOutput = Base64Url.encode(suppliedInput)
            val expectedEncodedOutput = it.second
            assertThat(actualEncodedOutput).isEqualTo(expectedEncodedOutput)
        }
    }

    @Test
    fun `rfc base64url decode vectors`() {
        base64UrlTestPairs.forEach {
            val expectedDecodedOutput = it.first.map { character -> character.toByte() }.toByteArray()
            val suppliedInput = it.second
            val actualDecodedOutput = Base64Url.decode(suppliedInput)
            assertEqualsByteArray(expectedDecodedOutput, actualDecodedOutput)
        }
    }

    @Test
    fun `twos compliment url`() {
        val suppliedInput = ByteArray(3)
        // CAFE41
        // 1100 1010 1111 1110 0100 0001
        // ^      ^       ^      ^
        // 50     47      57     1
        // y      v       5      B
        suppliedInput[0] = 0xCA.toByte()
        suppliedInput[1] = 0xFE.toByte()
        suppliedInput[2] = 0x41.toByte()
        val expectedOutput = "yv5B"
        val actualOutput = Base64Url.encode(suppliedInput)
        assertThat(actualOutput).isEqualTo(expectedOutput)

        val suppliedInputString = "1234"
        // 1(53) 2(54)  3(55)  4(56)
        // v     v      v      v
        // 11010111 01101101 11111000
        // 215      109      248
        val expectedOutputByteArray = ByteArray(3)
        expectedOutputByteArray[0] = 0xD7.toByte()
        expectedOutputByteArray[1] = 0x6D.toByte()
        expectedOutputByteArray[2] = 0xF8.toByte()
        val actualOutputByteArray = Base64Url.decode(suppliedInputString)
        assertEqualsByteArray(expectedOutputByteArray, actualOutputByteArray)
    }

    private fun assertEqualsByteArray(expected: ByteArray, actual: ByteArray) {
        assertThat(expected.size).isEqualTo(actual.size)
        expected.forEachIndexed { index, byte ->
            assertThat(byte).isEqualTo(actual[index])
        }
    }
}