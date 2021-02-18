// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

// only used for type
import android.content.Context
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.file.models.DifWordList
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test
import org.mockito.internal.matchers.Any
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupAndRestoreServiceTest {
    private val identityRepository: IdentifierRepository = mockk()
    private val keyStore: EncryptedKeyStore = mockk()
    private val context: Context = mockk()
    private val service = BackupAndRestoreService(identityRepository, keyStore, context)
    private val testWordSet = listOf("alpha", "bravo", "charlie", "delta", "echo",
        "foxtrot", "golf", "hotel", "india","juliett",
        "kilo", "lima", "mike", "november", "oscar",
        "papa", "quebec", "romeo", "sierra", "tango",
        "uniform", "victor", "whiskey", "xray", "yankee",
        "zulu"
    )

    @Before
    fun beforeEach() {
        mockkObject(DifWordList)
        every { DifWordList.getWordList(context)} returns (testWordSet)
    }

    @Test
    fun generateDifPasswordTest() {
        val generatedPassword = service.generateDifPassword()
        val passwordSet = generatedPassword.split(" ")
        assertTrue(passwordSet.count() > 0, "password must contain individual words")
        assertTrue(testWordSet.containsAll(passwordSet), "generated password contains additional words")
        assertEquals(passwordSet.count(), passwordSet.distinct().count(), "password contains duplicate words")
    }
}