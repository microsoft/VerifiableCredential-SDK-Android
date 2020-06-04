// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction
import com.microsoft.portableIdentity.sdk.repository.SdkDatabase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

class ReceiptDaoInstrumentedTest {
    @Rule
    @JvmField
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
        val suppliedReceipt = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val actualCardId = "urn:pic:testCardId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(actualCardId)
            actualReceipts.observeForever { assertThat(actualReceipts.value?.contains(suppliedReceipt)).isTrue() }
        }
    }

    @Test
    fun insertAndRetrieveAllReceiptsTest() {
        val suppliedReceipt = Receipt(
            id = 2,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val actualReceipts = receiptDao.getAllReceipts()
            actualReceipts.observeForever { assertThat(actualReceipts.value?.contains(suppliedReceipt)).isTrue() }
        }
    }

    @Test
    fun insertMultipleReceiptsForSameCardTest() {
        val suppliedReceipt1 = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        val suppliedReceipt2 = Receipt(
            id = 2,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt1)
            receiptDao.insert(suppliedReceipt2)
            val actualCardId = "urn:pic:testCardId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(actualCardId)
            actualReceipts.observeForever {
                assertThat(actualReceipts.value?.size).isEqualTo(2)
                assertThat(actualReceipts.value?.contains(suppliedReceipt1)).isTrue()
                assertThat(actualReceipts.value?.contains(suppliedReceipt2)).isTrue()
            }
        }
    }

    @Test
    fun insertMultipleReceiptsWithSameIdTest() {
        val suppliedReceipt1 = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        val suppliedReceipt2 = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt1)
            Assertions.assertThatThrownBy { runBlocking { receiptDao.insert(suppliedReceipt2) } }
                .isInstanceOf(android.database.sqlite.SQLiteConstraintException::class.java)
            val actualCardId = "urn:pic:testCardId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(actualCardId)
            actualReceipts.observeForever {
                assertThat(actualReceipts.value?.size).isEqualTo(1)
                assertThat(actualReceipts.value?.contains(suppliedReceipt1)).isTrue()
            }
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
            val actualCardId = ""
            val actualReceipts = receiptDao.getAllReceiptsByCardId(actualCardId)
            actualReceipts.observeForever { assertThat(actualReceipts.value?.contains(suppliedReceipt)).isTrue() }
        }
    }

    @Test
    fun insertAndRetrieveReceiptByNonExistingCardIdTest() {
        val suppliedReceipt = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val actualCardId = "nonExistingId"
            val actualReceipts = receiptDao.getAllReceiptsByCardId(actualCardId)
            actualReceipts.observeForever { assertThat(actualReceipts.value?.size).isEqualTo(0) }
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
            val actualCardId = ""
            val actualReceipts = receiptDao.getAllReceiptsByCardId(actualCardId)
            actualReceipts.observeForever { assertThat(actualReceipts.value?.first()).isEqualTo(suppliedReceipt) }
        }
    }

    @Test
    fun insertReceiptWithNoIdAndVerifyAutoGenerationOfIdTest() {
        val suppliedReceipt = Receipt(
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid",
            activityDate = 123445,
            entityName = "testEntityName",
            cardId = "urn:pic:testCardId"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt)
            val actualReceipts = receiptDao.getAllReceipts()
            actualReceipts.observeForever {
                assertThat(actualReceipts.value?.size).isEqualTo(1)
                assertThat(actualReceipts.value?.first()?.id).isEqualTo(1)
                assertThat(actualReceipts.value?.first()?.action).isEqualTo(suppliedReceipt.action)
                assertThat(actualReceipts.value?.first()?.entityIdentifier).isEqualTo(suppliedReceipt.entityIdentifier)
                assertThat(actualReceipts.value?.first()?.entityName).isEqualTo(suppliedReceipt.entityName)
                assertThat(actualReceipts.value?.first()?.activityDate).isEqualTo(suppliedReceipt.activityDate)
                assertThat(actualReceipts.value?.first()?.cardId).isEqualTo(suppliedReceipt.cardId)
            }
        }
    }

    @Test
    fun insertMultipleReceiptsAndVerifyAutoGenerationOfIdTest() {
        val suppliedReceipt1 = Receipt(
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid1",
            activityDate = 123445,
            entityName = "testEntityName1",
            cardId = "urn:pic:testCardId1"
        )
        val suppliedReceipt2 = Receipt(
            action = ReceiptAction.Presentation,
            entityIdentifier = "did:ion:test:testEntityDid2",
            activityDate = 123445,
            entityName = "testEntityName2",
            cardId = "urn:pic:testCardId2"
        )
        runBlocking {
            receiptDao.insert(suppliedReceipt1)
            receiptDao.insert(suppliedReceipt2)
            val actualReceipts = receiptDao.getAllReceipts()
            actualReceipts.observeForever {
                assertThat(actualReceipts.value?.size).isEqualTo(2)
                assertThat(actualReceipts.value?.first()?.id).isEqualTo(1)
                assertThat(actualReceipts.value?.first()?.action).isEqualTo(suppliedReceipt1.action)
                assertThat(actualReceipts.value?.first()?.entityIdentifier).isEqualTo(suppliedReceipt1.entityIdentifier)
                assertThat(actualReceipts.value?.first()?.entityName).isEqualTo(suppliedReceipt1.entityName)
                assertThat(actualReceipts.value?.first()?.activityDate).isEqualTo(suppliedReceipt1.activityDate)
                assertThat(actualReceipts.value?.first()?.cardId).isEqualTo(suppliedReceipt1.cardId)
                assertThat(actualReceipts.value?.last()?.id).isEqualTo(2)
                assertThat(actualReceipts.value?.last()?.action).isEqualTo(suppliedReceipt2.action)
                assertThat(actualReceipts.value?.last()?.entityIdentifier).isEqualTo(suppliedReceipt2.entityIdentifier)
                assertThat(actualReceipts.value?.last()?.entityName).isEqualTo(suppliedReceipt2.entityName)
                assertThat(actualReceipts.value?.last()?.activityDate).isEqualTo(suppliedReceipt2.activityDate)
                assertThat(actualReceipts.value?.last()?.cardId).isEqualTo(suppliedReceipt2.cardId)
            }
        }
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }
}