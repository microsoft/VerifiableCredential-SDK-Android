// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.repository.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.credential.service.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.credential.service.models.contracts.display.Logo
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialHolderDao
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.datasource.repository.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

class VerifiableCredentialHolderDaoInstrumentedTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var verifiableCredentialHolderDao: VerifiableCredentialHolderDao
    private var sdkDatabase: SdkDatabase

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkDatabase = Room.inMemoryDatabaseBuilder(context, SdkDatabase::class.java).build()
        verifiableCredentialHolderDao = sdkDatabase.verifiableCredentialHolderDao()
    }

    @Test
    fun insertAndRetrieveVchByIdTest() {
        val verifiableCredentialHolder = createVerifiableCredentialHolder(1)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder)
            val suppliedVcId = verifiableCredentialHolder.verifiableCredential.picId
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder)
        }
    }

    @Test
    fun insertAndRetrieveAllVchsTest() {
        val verifiableCredentialHolder = createVerifiableCredentialHolder(1)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder)
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getAllVcs().getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isNotNull
            if (actualVerifiableCredentialHolder != null)
                assertThat(actualVerifiableCredentialHolder.size).isEqualTo(1)
            assertThat(actualVerifiableCredentialHolder).contains(verifiableCredentialHolder)
        }
    }

    @Test
    fun insertMultipleVchsWithSameVcIdTest() {
        val verifiableCredentialHolder1 = createVerifiableCredentialHolder(1)
        val verifiableCredential = createVerifiableCredential(1)
        val identifier = createIdentifier(2)
        val displayContract = createDisplayContract()
        val verifiableCredentialHolder2 = VerifiableCredentialHolder("urn:vc:testVchsId2", verifiableCredential, identifier, displayContract)

        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder1)
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder2)
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getAllVcs().getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isNotNull
            if (actualVerifiableCredentialHolder != null)
                assertThat(actualVerifiableCredentialHolder.size).isEqualTo(2)
            assertThat(actualVerifiableCredentialHolder).contains(verifiableCredentialHolder1)
            assertThat(actualVerifiableCredentialHolder).contains(verifiableCredentialHolder2)
        }
    }

    @Test
    fun insertMultipleVchsWithSameVchIdFailingTest() {
        val verifiableCredentialHolder1 = createVerifiableCredentialHolder(1)
        val verifiableCredentialHolder2 = createVerifiableCredentialHolder(1)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder1)
            Assertions.assertThatThrownBy { runBlocking { verifiableCredentialHolderDao.insert(verifiableCredentialHolder2) } }
                .isInstanceOf(android.database.sqlite.SQLiteConstraintException::class.java)
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getAllVcs().getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isNotNull
            if(actualVerifiableCredentialHolder != null)
                assertThat(actualVerifiableCredentialHolder.size).isEqualTo(1)
            assertThat(actualVerifiableCredentialHolder).contains(verifiableCredentialHolder1)
        }
    }

    @Test
    fun insertVchWithEmptyVchIdTest() {
        val verifiableCredential = createVerifiableCredential(1)
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val verifiableCredentialHolder = VerifiableCredentialHolder("", verifiableCredential, identifier, displayContract)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder)
            val suppliedVcId = verifiableCredentialHolder.verifiableCredential.picId
            val actualverifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(actualverifiableCredentialHolder).isEqualTo(verifiableCredentialHolder)
        }
    }

    @Test
    fun insertVchWithEmptyVcIdTest() {
        val verifiableCredential = VerifiableCredential(
            "jti1",
            "raw",
            VerifiableCredentialContent(
                "jti1",
                VerifiableCredentialDescriptor(emptyList(), emptyList(), emptyMap()),
                "",
                "",
                123456L,
                200000L
            ),
            ""
        )
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val verifiableCredentialHolder = VerifiableCredentialHolder("urn:vc:testVchId1", verifiableCredential, identifier, displayContract)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder)
            val suppliedVcId = verifiableCredentialHolder.verifiableCredential.picId
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder)
        }
    }

    @Test
    fun retrieveVchByNonExistingVcIdTest() {
        runBlocking {
            val suppliedVchId = "nonExistingId"
            val actualReceipts = verifiableCredentialHolderDao.getVcById(suppliedVchId).getOrAwaitValue()
            assertThat(actualReceipts).isNull()
        }
    }

    @Test
    fun insertVchWithEmptyValuesTest() {
        val verifiableCredentialHolder = VerifiableCredentialHolder(
            "",
            VerifiableCredential(
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
            ),
            Identifier(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            ),
            DisplayContract(
                "", "", "",
                CardDescriptor(
                    "", "", "", "",
                    Logo("", "", ""), ""
                ),
                ConsentDescriptor("", ""), emptyMap()
            )
        )
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder)
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById("").getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder)
        }
    }

    @Test
    fun deleteVchTest() {
        val verifiableCredentialHolder = createVerifiableCredentialHolder(1)
        runBlocking {
            val suppliedVchId = verifiableCredentialHolder.verifiableCredential.picId
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder)
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVchId).getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder)
            verifiableCredentialHolderDao.delete(verifiableCredentialHolder)
            val deletedVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVchId).getOrAwaitValue()
            assertThat(deletedVerifiableCredentialHolder).isNull()
        }
    }

    @Test
    fun deleteNonExistingVchTest() {
        val verifiableCredentialHolder = createVerifiableCredentialHolder(1)
        runBlocking {
            val suppliedVchId = verifiableCredentialHolder.verifiableCredential.picId
            verifiableCredentialHolderDao.delete(verifiableCredentialHolder)
            val deletedVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVchId).getOrAwaitValue()
            assertThat(deletedVerifiableCredentialHolder).isNull()
        }
    }

    @Test
    fun deleteVchWithMatchingVchIdButDifferentVcIdTest() {
        val verifiableCredential = createVerifiableCredential(1)
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val verifiableCredentialHolder1 = VerifiableCredentialHolder("urn:vc:testVchId2", verifiableCredential, identifier, displayContract)
        val verifiableCredentialHolder2 = createVerifiableCredentialHolder(2)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder1)
            val suppliedVcId = verifiableCredentialHolder1.verifiableCredential.picId
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder1)
            verifiableCredentialHolderDao.delete(verifiableCredentialHolder2)
            val deletedVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(deletedVerifiableCredentialHolder).isNull()
        }
    }

    @Test
    fun deleteVchWithMatchingVcIdButDifferentVchIdTest() {
        val verifiableCredential = createVerifiableCredential(2)
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val verifiableCredentialHolder1 = VerifiableCredentialHolder("urn:vc:testVchId1", verifiableCredential, identifier, displayContract)
        val verifiableCredentialHolder2 = createVerifiableCredentialHolder(2)
        runBlocking {
            verifiableCredentialHolderDao.insert(verifiableCredentialHolder1)
            val suppliedVcId = verifiableCredentialHolder1.verifiableCredential.picId
            val actualVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(actualVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder1)
            verifiableCredentialHolderDao.delete(verifiableCredentialHolder2)
            val deletedVerifiableCredentialHolder = verifiableCredentialHolderDao.getVcById(suppliedVcId).getOrAwaitValue()
            assertThat(deletedVerifiableCredentialHolder).isEqualTo(verifiableCredentialHolder1)
        }
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }

    private fun createVerifiableCredentialHolder(id: Int): VerifiableCredentialHolder {
        val verifiableCredential = createVerifiableCredential(id)
        val identifier = createIdentifier(id)
        val displayContract = createDisplayContract()
        return VerifiableCredentialHolder("urn:vc:testVchId$id", verifiableCredential, identifier, displayContract)
    }

    private fun createVerifiableCredential(id: Int): VerifiableCredential {
        return VerifiableCredential(
            "jti$id",
            "raw",
            VerifiableCredentialContent(
                "jti$id",
                VerifiableCredentialDescriptor(emptyList(), emptyList(), emptyMap()),
                "",
                "",
                123456L,
                200000L
            ),
            "vcId$id"
        )
    }

    private fun createIdentifier(id: Int): Identifier {
        return Identifier(
            "did:ion:test:testId$id",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateKeyReference",
            "testIdentifierName$id"
        )
    }

    private fun createDisplayContract(): DisplayContract {
        val cardDescriptor =
            CardDescriptor(
                "title", "issuedBy", "backgroundColor", "textColor",
                Logo("uri", "image", "description"), "description"
            )
        val consentDescriptor = ConsentDescriptor("title", "instructions")
        val claims = emptyMap<String, ClaimDescriptor>()
        return DisplayContract("testDisplayContract", "testLocale", "testContract", cardDescriptor, consentDescriptor, claims)
    }
}