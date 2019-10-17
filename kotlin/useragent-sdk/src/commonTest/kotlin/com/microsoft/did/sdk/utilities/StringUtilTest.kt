package com.microsoft.did.sdk.utilities

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.expect

class StringUtilTest {

    @Test
    fun gauntletByBase64() {
        for (unused in 0..100) {
            val stringData = Base64.encode(Random.Default.nextBytes(24))

            val data = stringToByteArray(stringData)
            assertEquals(stringData, byteArrayToString(data))
        }
    }

    @Test
    fun gauntletByRandom() {
        for (unused in 0..100) {
            val data = Random.Default.nextBytes(24)

            val stringData = byteArrayToString(data)
            val actualData = stringToByteArray(stringData)
            data.forEachIndexed{
                index, expectedByte ->
                assertEquals(expectedByte, actualData[index])
            }
        }
    }
}