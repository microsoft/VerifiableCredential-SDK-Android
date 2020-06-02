// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.MutableLiveData
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.portableIdentity.sdk.PortableIdentitySdk
import com.microsoft.portableIdentity.sdk.auth.models.contracts.display.*
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialDescriptor
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.dao.PortableIdentityCardDao
import com.microsoft.portableIdentity.sdk.repository.dao.ReceiptDao
import com.microsoft.portableIdentity.sdk.repository.dao.VerifiableCredentialDao
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class CardRepositoryInstrumentedTest {
    private val cardRepository: CardRepository

    init {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PortableIdentitySdk.init(context)
        cardRepository = spyk(PortableIdentitySdk.cardManager.picRepository)
    }

    @Test
    fun picInsertTest() {
        val portableIdentityCard = createPortableIdentityCard(1)
        val cardDao: PortableIdentityCardDao = mockk()
        coJustRun { cardDao.insert(portableIdentityCard) }
        runBlocking(Dispatchers.Main) {
            cardRepository.insert(portableIdentityCard)
            val insertedCard = cardRepository.getCardById(portableIdentityCard.verifiableCredential.picId)
            insertedCard.observeForever { assertThat(insertedCard?.value).isEqualToComparingFieldByFieldRecursively(portableIdentityCard) }
        }
    }

    @Test
    fun picDeleteTest() {
        val portableIdentityCard = createPortableIdentityCard(2)
        val cardDao: PortableIdentityCardDao = mockk()
        coJustRun { cardDao.insert(portableIdentityCard) }
        coJustRun { cardDao.delete(portableIdentityCard) }
        runBlocking(Dispatchers.Main) {
            cardRepository.insert(portableIdentityCard)
            cardRepository.delete(portableIdentityCard)
            val insertedCard = cardRepository.getCardById(portableIdentityCard.verifiableCredential.picId)
            insertedCard.observeForever { assertThat(insertedCard?.value).isNull() }
        }
    }

    @Test
    fun getPicTest() {
        val portableIdentityCard = createPortableIdentityCard(3)
        val expectedCardList = MutableLiveData<List<PortableIdentityCard>>()
        expectedCardList.postValue(listOf(portableIdentityCard))
        val cardDao: PortableIdentityCardDao = mockk()
        coJustRun { cardDao.insert(portableIdentityCard) }
        every { cardDao.getAllCards() } returns expectedCardList
        runBlocking(Dispatchers.Main) {
            cardRepository.insert(portableIdentityCard)
            val actualCardList = cardRepository.getAllCards()
            actualCardList.observeForever { assertThat(actualCardList?.value).containsAll(expectedCardList.value) }
        }
    }

    @Test
    fun vcInsertTest() {
        val verifiableCredential = createVerifiableCredential(4)
        val verifiableCredentialDao: VerifiableCredentialDao = mockk()
        coJustRun { verifiableCredentialDao.insert(verifiableCredential) }
        runBlocking {
            cardRepository.insert(verifiableCredential)
            val insertedVcs = cardRepository.getAllVerifiableCredentialsByCardId(verifiableCredential.picId)
            assertThat(insertedVcs).contains(verifiableCredential)
        }
    }

    @Test
    fun getVcByNonExistingIdTest() {
        val verifiableCredential = createVerifiableCredential(5)
        runBlocking {
            val insertedVcs = cardRepository.getAllVerifiableCredentialsByCardId(verifiableCredential.picId)
            assertThat(insertedVcs.size).isEqualTo(0)
        }
    }

    @Test
    fun receiptInsertTest() {
        val receipt = Receipt(
            id = 1,
            action = ReceiptAction.Presentation,
            cardId = "testCardId",
            activityDate = Calendar.getInstance().timeInMillis,
            entityIdentifier = "testEntityDid",
            entityName = "testEntityName"
        )
        val receiptDao: ReceiptDao = mockk()
        coJustRun { receiptDao.insert(receipt) }
        runBlocking(Dispatchers.Main) {
            cardRepository.insert(receipt)
            val allReceiptsForCardId = cardRepository.getAllReceiptsByCardId(receipt.cardId)
            allReceiptsForCardId.observeForever { assertThat(allReceiptsForCardId?.value).contains(receipt) }
        }
    }

    private fun createPortableIdentityCard(id: Int): PortableIdentityCard {
        val verifiableCredential = createVerifiableCredential(id)
        val identifier = Identifier(
            "did:ion:test:testId$id",
            "testAlias",
            "testSigningKeyReference",
            "testEncryptionKeyReference",
            "testRecoveryKeyReference",
            "testUpdateRevealValue",
            "testRecoveryRevealValue",
            "testIdentifierName$id"
        )
        val cardDescriptor =
            CardDescriptor(
                "title", "issuedBy", "backgroundColor", "textColor",
                Logo("uri", "image", "description"), "description"
            )
        val consentDescriptor = ConsentDescriptor("title", "instructions")
        val claims = emptyMap<String, ClaimDescriptor>()
        val displayContract =
            DisplayContract("testDisplayContract", "testLocale", "testContract", cardDescriptor, consentDescriptor, claims)
        return PortableIdentityCard("testCardId$id", verifiableCredential, identifier, displayContract)
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
}