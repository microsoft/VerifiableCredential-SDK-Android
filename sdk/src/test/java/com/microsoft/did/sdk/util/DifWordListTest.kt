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
        DifWordList.wordList = testWordSet
        val passwordSet = DifWordList.generateDifPassword().split(" ")
        assertTrue(passwordSet.count() > 0, "password must contain individual words")
        assertTrue(testWordSet.containsAll(passwordSet), "generated password contains additional words")
        assertEquals(passwordSet.count(), passwordSet.distinct().count(), "password contains duplicate words")
    }
}