// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.repository.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.auth.models.contracts.display.CardDescriptor
import com.microsoft.did.sdk.auth.models.contracts.display.ClaimDescriptor
import com.microsoft.did.sdk.auth.models.contracts.display.ConsentDescriptor
import com.microsoft.did.sdk.auth.models.contracts.display.DisplayContract
import com.microsoft.did.sdk.auth.models.contracts.display.Logo
import com.microsoft.did.sdk.cards.PortableIdentityCard
import com.microsoft.did.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.did.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.did.sdk.cards.verifiableCredential.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.repository.SdkDatabase
import com.microsoft.did.sdk.repository.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test

class PortableIdentityCardDaoInstrumentedTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private var portableIdentityCardDao: PortableIdentityCardDao
    private var sdkDatabase: SdkDatabase

    init {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        sdkDatabase = Room.inMemoryDatabaseBuilder(context, SdkDatabase::class.java).build()
        portableIdentityCardDao = sdkDatabase.cardDao()
    }

    @Test
    fun insertAndRetrieveCardByIdTest() {
        val portableIdentityCard = createPortableIdentityCard(1)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard)
            val suppliedPicId = portableIdentityCard.verifiableCredential.picId
            val actualCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard)
        }
    }

    @Test
    fun insertAndRetrieveAllCardsTest() {
        val portableIdentityCard = createPortableIdentityCard(1)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard)
            val actualCard = portableIdentityCardDao.getAllCards().getOrAwaitValue()
            assertThat(actualCard.size).isEqualTo(1)
            assertThat(actualCard).contains(portableIdentityCard)
        }
    }

    @Test
    fun insertMultipleCardsWithSamePicIdTest() {
        val portableIdentityCard1 = createPortableIdentityCard(1)
        val verifiableCredential = createVerifiableCredential(1)
        val identifier = createIdentifier(2)
        val displayContract = createDisplayContract()
        val portableIdentityCard2 = PortableIdentityCard("urn:pic:testCardId2", verifiableCredential, identifier, displayContract)

        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard1)
            portableIdentityCardDao.insert(portableIdentityCard2)
            val actualCard = portableIdentityCardDao.getAllCards().getOrAwaitValue()
            assertThat(actualCard.size).isEqualTo(2)
            assertThat(actualCard).contains(portableIdentityCard1)
            assertThat(actualCard).contains(portableIdentityCard2)
        }
    }

    @Test
    fun insertMultipleCardsWithSameCardIdFailingTest() {
        val portableIdentityCard1 = createPortableIdentityCard(1)
        val portableIdentityCard2 = createPortableIdentityCard(1)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard1)
            Assertions.assertThatThrownBy { runBlocking { portableIdentityCardDao.insert(portableIdentityCard2) } }
                .isInstanceOf(android.database.sqlite.SQLiteConstraintException::class.java)
            val actualCard = portableIdentityCardDao.getAllCards().getOrAwaitValue()
            assertThat(actualCard.size).isEqualTo(1)
            assertThat(actualCard).contains(portableIdentityCard1)
        }
    }

    @Test
    fun insertCardWithEmptyCardIdTest() {
        val verifiableCredential = createVerifiableCredential(1)
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val portableIdentityCard = PortableIdentityCard("", verifiableCredential, identifier, displayContract)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard)
            val suppliedPicId = portableIdentityCard.verifiableCredential.picId
            val actualCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard)
        }
    }

    @Test
    fun insertCardWithEmptyPicIdTest() {
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
        val portableIdentityCard = PortableIdentityCard("urn:pic:testCardId1", verifiableCredential, identifier, displayContract)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard)
            val suppliedPicId = portableIdentityCard.verifiableCredential.picId
            val actualCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard)
        }
    }

    @Test
    fun retrieveCardByNonExistingPicIdTest() {
        runBlocking {
            val suppliedCardId = "nonExistingId"
            val actualReceipts = portableIdentityCardDao.getCardById(suppliedCardId).getOrAwaitValue()
            assertThat(actualReceipts).isNull()
        }
    }

    @Test
    fun insertCardWithEmptyValuesTest() {
        val portableIdentityCard = PortableIdentityCard(
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
            portableIdentityCardDao.insert(portableIdentityCard)
            val actualCard = portableIdentityCardDao.getCardById("").getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard)
        }
    }

    @Test
    fun deleteCardTest() {
        val portableIdentityCard = createPortableIdentityCard(1)
        runBlocking {
            val suppliedCardId = portableIdentityCard.verifiableCredential.picId
            portableIdentityCardDao.insert(portableIdentityCard)
            val actualCard = portableIdentityCardDao.getCardById(suppliedCardId).getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard)
            portableIdentityCardDao.delete(portableIdentityCard)
            val deletedCard = portableIdentityCardDao.getCardById(suppliedCardId).getOrAwaitValue()
            assertThat(deletedCard).isNull()
        }
    }

    @Test
    fun deleteNonExistingCardTest() {
        val portableIdentityCard = createPortableIdentityCard(1)
        runBlocking {
            val suppliedCardId = portableIdentityCard.verifiableCredential.picId
            portableIdentityCardDao.delete(portableIdentityCard)
            val deletedCard = portableIdentityCardDao.getCardById(suppliedCardId).getOrAwaitValue()
            assertThat(deletedCard).isNull()
        }
    }

    @Test
    fun deleteCardWithMatchingCardIdButDifferentPicIdTest() {
        val verifiableCredential = createVerifiableCredential(1)
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val portableIdentityCard1 = PortableIdentityCard("urn:pic:testCardId2", verifiableCredential, identifier, displayContract)
        val portableIdentityCard2 = createPortableIdentityCard(2)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard1)
            val suppliedPicId = portableIdentityCard1.verifiableCredential.picId
            val actualCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard1)
            portableIdentityCardDao.delete(portableIdentityCard2)
            val deletedCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(deletedCard).isNull()
        }
    }

    @Test
    fun deleteCardWithMatchingPicIdButDifferentCardIdTest() {
        val verifiableCredential = createVerifiableCredential(2)
        val identifier = createIdentifier(1)
        val displayContract = createDisplayContract()
        val portableIdentityCard1 = PortableIdentityCard("urn:pic:testCardId1", verifiableCredential, identifier, displayContract)
        val portableIdentityCard2 = createPortableIdentityCard(2)
        runBlocking {
            portableIdentityCardDao.insert(portableIdentityCard1)
            val suppliedPicId = portableIdentityCard1.verifiableCredential.picId
            val actualCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(actualCard).isEqualTo(portableIdentityCard1)
            portableIdentityCardDao.delete(portableIdentityCard2)
            val deletedCard = portableIdentityCardDao.getCardById(suppliedPicId).getOrAwaitValue()
            assertThat(deletedCard).isEqualTo(portableIdentityCard1)
        }
    }

    @After
    fun tearDown() {
        sdkDatabase.close()
    }

    private fun createPortableIdentityCard(id: Int): PortableIdentityCard {
        val verifiableCredential = createVerifiableCredential(id)
        val identifier = createIdentifier(id)
        val displayContract = createDisplayContract()
        return PortableIdentityCard("urn:pic:testCardId$id", verifiableCredential, identifier, displayContract)
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
            "picId$id"
        )
    }

    private fun createIdentifier(id: Int): Identifier {
        return Identifier(
            "did:ion:test:testId$id",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
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