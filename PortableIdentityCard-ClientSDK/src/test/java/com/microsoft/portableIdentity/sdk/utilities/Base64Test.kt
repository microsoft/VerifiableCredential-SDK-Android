package com.microsoft.portableIdentity.sdk.utilities

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat

class Base64Test {
    private val logger = ConsoleLogger()
    /**
     * @see https://tools.ietf.org/html/rfc4648#section-10
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
    fun `rfc encode vectors`() {
        base64TestPairs.forEach {
            val inputDataList = it.first.map { character -> character.toByte() }
            val inputData = ByteArray(inputDataList.size)
            inputDataList.forEachIndexed { index, byte -> inputData[index] = byte }
            val expectedOutput = Base64.encode(inputData, logger)
            assertThat(it.second).isEqualTo(expectedOutput)
        }
    }

    @Test
    fun `rfc decode vectors`() {
        base64TestPairs.forEach {
            val outputData = it.first.map { character -> character.toByte() }.toByteArray()
            val output = Base64.decode(it.second, logger)
            assertEqualsByteArray(outputData, output)
        }
    }

    @Test
    fun `rfc url encode vectors`() {
        base64UrlTestPairs.forEach {
            val inputDataList = it.first.map { character -> character.toByte() }
            val inputData = ByteArray(inputDataList.size)
            inputDataList.forEachIndexed { index, byte -> inputData[index] = byte }
            val expectedOutput = Base64Url.encode(inputData, logger)
            assertThat(it.second).isEqualTo(expectedOutput)
        }
    }

    @Test
    fun `rfc url decode vectors`() {
        base64UrlTestPairs.forEach {
            val outputData = it.first.map { character -> character.toByte() }.toByteArray()
            val output = Base64Url.decode(it.second, logger)
            assertEqualsByteArray(outputData, output)
        }
    }

    @Test
    fun `twos compliment url`() {
        val data = ByteArray(3)
        // CAFE41
        // 1100 1010 1111 1110 0100 0001
        // ^      ^       ^      ^
        // 50     47      57     1
        // y      v       5      B
        data[0] = 0xCA.toByte()
        data[1] = 0xFE.toByte()
        data[2] = 0x41.toByte()

        assertThat("yv5B").isEqualTo(Base64Url.encode(data, logger))

        val stringData = "1234"
        // 1(53) 2(54)  3(55)  4(56)
        // v     v      v      v
        // 11010111 01101101 11111000
        // 215      109      248
        val expectedData = ByteArray(3)
        expectedData[0] = 0xD7.toByte()
        expectedData[1] = 0x6D.toByte()
        expectedData[2] = 0xF8.toByte()
        assertEqualsByteArray(expectedData, Base64Url.decode(stringData, logger))
    }

    private fun assertEqualsByteArray(expected: ByteArray, actual: ByteArray) {
        assertThat(expected.size).isEqualTo(actual.size)
        expected.forEachIndexed { index, byte ->
            assertThat(byte).isEqualTo(actual[index])
        }
    }
}