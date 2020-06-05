// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction
import com.microsoft.portableIdentity.sdk.repository.SdkDatabase
import com.microsoft.portableIdentity.sdk.repository.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

class ReceiptDaoInstrumentedTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var receiptDao: ReceiptDao
    private var sdkDatabase: SdkDatabase

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkDatabase = Room.inMemoryDatabaseBuilder(context, SdkDatabase::class.java).build()
        receiptDao = sdkDatabase.receiptDao()
    }

    @Test
    fun insertAndRetrieveReceiptByCardIdTest() {
        val suppliedReceipt = createReceipt(1)
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val suppliedCardId = "urn:pic:testCardId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts.contains(suppliedReceipt)).isTrue()
        }
    }

    @Test
    fun insertAndRetrieveAllReceiptsTest() {
        val suppliedReceipt = createReceipt(2)
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val actualReceipts = receiptDao.getAllReceipts().getOrAwaitValue()
            assertThat(actualReceipts.contains(suppliedReceipt)).isTrue()
        }
    }

    @Test
    fun insertMultipleReceiptsForSameCardIdTest() {
        val suppliedReceipt1 = createReceipt(1)
        val suppliedReceipt2 = createReceipt(2)
        runBlocking {
            receiptDao.insert(suppliedReceipt1)
            receiptDao.insert(suppliedReceipt2)
            val suppliedCardId = "urn:pic:testCardId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts.size).isEqualTo(2)
            assertThat(actualReceipts.contains(suppliedReceipt1)).isTrue()
            assertThat(actualReceipts.contains(suppliedReceipt2)).isTrue()
        }
    }

    @Test
    fun insertMultipleReceiptsWithSameIdFailingTest() {
        val suppliedReceipt1 = createReceipt(1)
        val suppliedReceipt2 = createReceipt(1)
        runBlocking {
            receiptDao.insert(suppliedReceipt1)
            Assertions.assertThatThrownBy { runBlocking { receiptDao.insert(suppliedReceipt2) } }
                .isInstanceOf(android.database.sqlite.SQLiteConstraintException::class.java)
            val suppliedCardId = "urn:pic:testCardId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts.size).isEqualTo(1)
            assertThat(actualReceipts.contains(suppliedReceipt1)).isTrue()
        }
    }

    @Test
    fun insertReceiptWithEmptyCardIdTest() {
        val suppliedReceipt = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = ""
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val suppliedCardId = ""
            val actualReceipts = receiptDao.getAllReceiptsByCardId(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts.contains(suppliedReceipt)).isTrue()
        }
    }

    @Test
    fun retrieveReceiptByNonExistingCardIdTest() {
        runBlocking {
            val suppliedCardId = "nonExistingId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts.size).isEqualTo(0)
        }
    }

    @Test
    fun insertReceiptWithEmptyValuesTest() {
        val suppliedReceipt = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "",
            activityDate = 0,
            entityName = "",
            cardId = ""
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val suppliedCardId = ""
            val actualReceipts = receiptDao.getAllReceiptsByCardId(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts.first()).isEqualTo(suppliedReceipt)
        }
    }

    @Test
    fun insertReceiptWithNoIdAndVerifyAutoGenerationOfIdTest() {
        val suppliedReceipt = createReceiptWithNoId(1)
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val actualReceipts = receiptDao.getAllReceipts().getOrAwaitValue()
            assertThat(actualReceipts.size).isEqualTo(1)
            val actualReceipt = actualReceipts.first()
            assertThat(actualReceipt.id).isEqualTo(1)
            assertThat(actualReceipt.action).isEqualTo(suppliedReceipt.action)
            assertThat(actualReceipt.entityIdentifier).isEqualTo(suppliedReceipt.entityIdentifier)
            assertThat(actualReceipt.entityName).isEqualTo(suppliedReceipt.entityName)
            assertThat(actualReceipt.activityDate).isEqualTo(suppliedReceipt.activityDate)
            assertThat(actualReceipt.cardId).isEqualTo(suppliedReceipt.cardId)
        }
    }

    @Test
    fun insertMultipleReceiptsAndVerifyAutoGenerationOfIdTest() {
        val suppliedReceipt1 = createReceiptWithNoId(1)
        val suppliedReceipt2 = createReceiptWithNoId(2)
        runBlocking {
            receiptDao.insert(suppliedReceipt1)
            receiptDao.insert(suppliedReceipt2)
            val actualReceipts = receiptDao.getAllReceipts().getOrAwaitValue()
            val actualReceipt1 = actualReceipts.first()
            val actualReceipt2 = actualReceipts.last()
            assertThat(actualReceipts.size).isEqualTo(2)
            assertThat(actualReceipt1.id).isEqualTo(1)
            assertThat(actualReceipt1.action).isEqualTo(suppliedReceipt1.action)
            assertThat(actualReceipt1.entityIdentifier).isEqualTo(suppliedReceipt1.entityIdentifier)
            assertThat(actualReceipt1.entityName).isEqualTo(suppliedReceipt1.entityName)
            assertThat(actualReceipt1.activityDate).isEqualTo(suppliedReceipt1.activityDate)
            assertThat(actualReceipt1.cardId).isEqualTo(suppliedReceipt1.cardId)
            assertThat(actualReceipt2.id).isEqualTo(2)
            assertThat(actualReceipt2.action).isEqualTo(suppliedReceipt2.action)
            assertThat(actualReceipt2.entityIdentifier).isEqualTo(suppliedReceipt2.entityIdentifier)
            assertThat(actualReceipt2.entityName).isEqualTo(suppliedReceipt2.entityName)
            assertThat(actualReceipt2.activityDate).isEqualTo(suppliedReceipt2.activityDate)
            assertThat(actualReceipt2.cardId).isEqualTo(suppliedReceipt2.cardId)
        }
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }

    private fun createReceipt(id: Int): Receipt {
        return Receipt(
            id = id,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
    }

    private fun createReceiptWithNoId(id:Int): Receipt {
        return Receipt(
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid$id",
            activityDate = 123445,
            entityName = "testEntityName$id",
            cardId = "urn:pic:testCardId$id"
        )
    }
}