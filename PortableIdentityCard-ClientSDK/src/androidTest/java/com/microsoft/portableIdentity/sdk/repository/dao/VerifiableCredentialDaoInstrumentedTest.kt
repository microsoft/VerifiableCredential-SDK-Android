// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.dao

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialDescriptor
import com.microsoft.portableIdentity.sdk.repository.SdkDatabase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test

class VerifiableCredentialDaoInstrumentedTest {

    private var verifiableCredentialDao: VerifiableCredentialDao
    private var sdkDatabase: SdkDatabase

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkDatabase = Room.inMemoryDatabaseBuilder(context, SdkDatabase::class.java).build()
        verifiableCredentialDao = sdkDatabase.verifiableCredentialDao()
    }

    @Test
    fun insertVCAndRetrieveTest() {
        val suppliedVc = createVerifiableCredential(1)
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualPicId = "picId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun insertMultipleVcWithSamePicId() {
        val suppliedVc1 = createVerifiableCredential(1)
        val suppliedVc2 = VerifiableCredential(
            "jti2",
            "raw",
            createVerifiableCredentialContent(1),
            "picId1"
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc1)
            verifiableCredentialDao.insert(suppliedVc2)
            val actualPicId = "picId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc).contains(suppliedVc1)
            assertThat(actualVc).contains(suppliedVc2)
        }
    }

    @Test
    fun insertVCWithDifferentNestedIdTest() {
        val suppliedVc = VerifiableCredential(
            "jti1",
            "raw",
            createVerifiableCredentialContent(2),
            "picId1"
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualPicId = "picId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun insertMultipleVcsWithSameIdFailingTest() {
        val suppliedVc1 = createVerifiableCredential(1)
        val suppliedVc2 = createVerifiableCredential(1)
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc1)
            Assertions.assertThatThrownBy { runBlocking { verifiableCredentialDao.insert(suppliedVc2) } }
                .isInstanceOf(android.database.sqlite.SQLiteConstraintException::class.java)
            val actualPicId = "picId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc.size).isEqualTo(1)
            assertThat(actualVc).contains(suppliedVc1)
        }
    }

    @Test
    fun insertVcWithEmptyPicIdTest() {
        val suppliedVc = VerifiableCredential(
            "jti1",
            "raw",
            createVerifiableCredentialContent(1),
            ""
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualPicId = ""
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun retrieveVcByNonExistingPicIdTest() {
        runBlocking {
            val actualPicId = "nonExistingPicId"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc.size).isEqualTo(0)
        }
    }

    @Test
    fun insertVcWithEmptyValuesTest() {
        val suppliedVc = VerifiableCredential(
            "",
            "",
            VerifiableCredentialContent(
                "",
                VerifiableCredentialDescriptor(emptyList(), emptyList(), emptyMap()),
                "",
                "",
                0,
                0
            ),
            ""
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualPicId = ""
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun deleteVcTest() {
        val suppliedVc = createVerifiableCredential(1)
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualPicId = "picId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc).contains(suppliedVc)
            verifiableCredentialDao.delete(suppliedVc)
            val deletedVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(deletedVc.size).isEqualTo(0)
        }
    }

    @Test
    fun deleteNonExistingVcTest() {
        val suppliedVc = createVerifiableCredential(1)
        runBlocking {
            verifiableCredentialDao.delete(suppliedVc)
            val actualPicId = "picId"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc.size).isEqualTo(0)
        }
    }

    @Test
    fun deleteVcWithMatchingJtiButDifferentPicIdTest() {
        val suppliedVc = createVerifiableCredential(1)
        val vcToBeDeleted = VerifiableCredential(
            "jti1",
            "raw1",
            createVerifiableCredentialContent(2),
            "picId2"
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualPicId = "picId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(actualVc.size).isEqualTo(1)
            assertThat(actualVc).contains(suppliedVc)
            verifiableCredentialDao.delete(vcToBeDeleted)
            val deletedVc = verifiableCredentialDao.getVerifiableCredentialByCardId(actualPicId)
            assertThat(deletedVc.size).isEqualTo(0)
        }
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }

    private fun createVerifiableCredential(id: Int): VerifiableCredential {
        return VerifiableCredential(
            "jti$id",
            "raw",
            createVerifiableCredentialContent(id),
            "picId$id"
        )
    }

    private fun createVerifiableCredentialContent(id: Int): VerifiableCredentialContent {
        return VerifiableCredentialContent(
            "jti$id",
            VerifiableCredentialDescriptor(emptyList(), emptyList(), emptyMap()),
            "",
            "",
            123456L,
            200000L
        )
    }
}