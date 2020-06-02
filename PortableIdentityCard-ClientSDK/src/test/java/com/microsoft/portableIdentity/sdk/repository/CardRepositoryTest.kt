// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository

import android.content.Context
import com.microsoft.portableIdentity.sdk.PortableIdentitySdk
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.repository.dao.PortableIdentityCardDao
import com.microsoft.portableIdentity.sdk.repository.dao.ReceiptDao
import com.microsoft.portableIdentity.sdk.repository.dao.VerifiableCredentialDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CardRepositoryTest {
    private val portableIdentityCard: PortableIdentityCard = mockk()
    private val receipt: Receipt = mockk()
    private val verifiableCredential: VerifiableCredential = mockk()
    private val cardRepository: CardRepository

    init {
        val context: Context = mockk()
        PortableIdentitySdk.init(context)
        cardRepository = PortableIdentitySdk.cardManager.picRepository

        val receiptDao: ReceiptDao = mockk()
        val verifiableCredentialDao: VerifiableCredentialDao = mockk()

        coEvery { receiptDao.insert(receipt) } returns Unit
        coEvery { verifiableCredentialDao.insert(verifiableCredential) } returns Unit
    }

/*    @Test
    fun `test insert portable identity card`() {
        val cardDao: PortableIdentityCardDao = mockk()
        coEvery { cardDao.insert(portableIdentityCard) } returns Unit
        runBlocking {
            cardRepository.insert(portableIdentityCard)
            val insertedCard = cardRepository.getCardById(portableIdentityCard.cardId)
        }
    }*/
}