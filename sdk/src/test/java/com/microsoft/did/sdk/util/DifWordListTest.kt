// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.util

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DifWordListTest {
    private val testWordSet = listOf(
        "alpha", "bravo", "charlie", "delta", "echo",
        "foxtrot", "golf", "hotel", "india", "juliett",
        "kilo", "lima", "mike", "november", "oscar",
        "papa", "quebec", "romeo", "sierra", "tango",
        "uniform", "victor", "whiskey", "xray", "yankee",
        "zulu"
    )

    @Test
    fun generateDifPasswordTest() {
        repeat(20) {
            DifWordList.wordList = testWordSet
            val passwordSet = DifWordList.generateDifPassword().split(" ")
            assertEquals(passwordSet.count(), Constants.PASSWORD_SET_SIZE, "password does not contain the right number of words ${passwordSet.count()}")
            assertTrue(testWordSet.containsAll(passwordSet), "generated password contains additional words")
            assertEquals(passwordSet.count(), passwordSet.distinct().count(), "password contains duplicate words")
        }
    }
}