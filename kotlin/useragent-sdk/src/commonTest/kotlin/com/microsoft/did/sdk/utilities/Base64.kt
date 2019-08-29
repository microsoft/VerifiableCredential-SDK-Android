package com.microsoft.did.sdk.utilities

import kotlin.test.Test
import kotlin.test.assertEquals

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

@Test fun rfcTestVectors() {
    base64TestPairs.forEach {
        val inputDataList = it.first.map { character -> character.toByte() }
        val inputData = ByteArray(inputDataList.size)
        inputDataList.forEachIndexed {index, byte -> inputData[index] = byte }
        val output = Base64.encode(inputData)
        assertEquals(it.second, output, "Failed to encode \"${it.first}\" correctly.")
    }
}