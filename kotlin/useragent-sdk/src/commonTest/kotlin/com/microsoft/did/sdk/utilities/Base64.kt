package com.microsoft.did.sdk.utilities

import kotlin.test.Test
import kotlin.test.assertEquals


class Base64TestSuite() {
    /**
     * @see https://tools.ietf.org/html/rfc4648#section-10
     */
    val base64TestPairs = listOf(
        Pair("", ""),
        Pair("f", "Zg=="),
        Pair("fo", "Zm8="),
        Pair("foo", "Zm9v"),
        Pair("foob", "Zm9vYg=="),
        Pair("fooba", "Zm9vYmE="),
        Pair("foobar", "Zm9vYmFy")
    )

    val base64UrlTestPairs = listOf(
        Pair("", ""),
        Pair("f", "Zg"),
        Pair("fo", "Zm8"),
        Pair("foo", "Zm9v"),
        Pair("foob", "Zm9vYg"),
        Pair("fooba", "Zm9vYmE"),
        Pair("foobar", "Zm9vYmFy")
    )

    @Test
    fun rfcEncodeVectorsTest() {
        base64TestPairs.forEach {
            val inputDataList = it.first.map { character -> character.toByte() }
            val inputData = ByteArray(inputDataList.size)
            inputDataList.forEachIndexed {index, byte -> inputData[index] = byte }
            val output = Base64.encode(inputData)
            assertEquals(it.second, output, "Failed to encode \"${it.first}\" correctly.")
        }
    }

    @Test
    fun rfcDecodeVectorsTest() {
        base64TestPairs.forEach {
            val outputData = it.first.map { character -> character.toByte() }.toByteArray()
            val output = Base64.decode(it.second)
            assertEqualsByteArray(outputData, output, "Failed to decode \"${it.first}\" correctly.")
        }
    }

    @Test
    fun rfcUrlEncodeVectorsTest() {
        base64UrlTestPairs.forEach {
            val inputDataList = it.first.map { character -> character.toByte() }
            val inputData = ByteArray(inputDataList.size)
            inputDataList.forEachIndexed {index, byte -> inputData[index] = byte }
            val output = Base64Url.encode(inputData)
            assertEquals(it.second, output, "Failed to encode \"${it.first}\" correctly.")
        }
    }

    @Test
    fun rfcUrlDecodeVectorsTest() {
        base64UrlTestPairs.forEach {
            val outputData = it.first.map { character -> character.toByte() }.toByteArray()
            val output = Base64Url.decode(it.second)
            assertEqualsByteArray(outputData, output, "Failed to decode \"${it.first}\" correctly.")
        }
    }

    fun assertEqualsByteArray(expected: ByteArray, actual: ByteArray, message: String = "ByteArrays did not match") {
        assertEquals(expected.size, actual.size, "ByteArrays are of different length.")
        expected.forEachIndexed{
            index, byte ->
            assertEquals(byte, actual[index], message)
        }
    }
}