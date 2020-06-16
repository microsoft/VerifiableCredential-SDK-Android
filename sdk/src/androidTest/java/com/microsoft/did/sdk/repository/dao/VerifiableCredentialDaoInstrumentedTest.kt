// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.repository.dao

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialDao
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
            val actualVchId = "vchId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun insertMultipleVcWithSameVchId() {
        val suppliedVc1 = createVerifiableCredential(1)
        val suppliedVc2 = VerifiableCredential(
            "jti2",
            "raw",
            createVerifiableCredentialContent(1),
            "vchId1"
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc1)
            verifiableCredentialDao.insert(suppliedVc2)
            val actualVchId = "vchId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
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
            "vchId1"
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualVchId = "vchId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
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
            val actualVchId = "vchId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc.size).isEqualTo(1)
            assertThat(actualVc).contains(suppliedVc1)
        }
    }

    @Test
    fun insertVcWithEmptyVchIdTest() {
        val suppliedVc = VerifiableCredential(
            "jti1",
            "raw",
            createVerifiableCredentialContent(1),
            ""
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualVchId = ""
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun retrieveVcByNonExistingVchIdTest() {
        runBlocking {
            val actualVchId = "nonExistingVchId"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
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
            val actualVchId = ""
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc).contains(suppliedVc)
        }
    }

    @Test
    fun deleteVcTest() {
        val suppliedVc = createVerifiableCredential(1)
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualVchId = "vchId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc).contains(suppliedVc)
            verifiableCredentialDao.delete(suppliedVc)
            val deletedVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(deletedVc.size).isEqualTo(0)
        }
    }

    @Test
    fun deleteNonExistingVcTest() {
        val suppliedVc = createVerifiableCredential(1)
        runBlocking {
            verifiableCredentialDao.delete(suppliedVc)
            val actualVchId = "vchId"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc.size).isEqualTo(0)
        }
    }

    @Test
    fun deleteVcWithMatchingJtiButDifferentVchIdTest() {
        val suppliedVc = createVerifiableCredential(1)
        val vcToBeDeleted = VerifiableCredential(
            "jti1",
            "raw1",
            createVerifiableCredentialContent(2),
            "vchId2"
        )
        runBlocking {
            verifiableCredentialDao.insert(suppliedVc)
            val actualVchId = "vchId1"
            val actualVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
            assertThat(actualVc.size).isEqualTo(1)
            assertThat(actualVc).contains(suppliedVc)
            verifiableCredentialDao.delete(vcToBeDeleted)
            val deletedVc = verifiableCredentialDao.getVerifiableCredentialById(actualVchId)
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
            "vchId$id"
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
