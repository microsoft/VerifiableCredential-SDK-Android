// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.validators.PresentationRequestValidator
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardManagerTest {
    private val cardRepository: CardRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val cardManager = spyk(CardManager(cardRepository, serializer, presentationRequestValidator), recordPrivateCalls=true)
    private val issuanceRequest: IssuanceRequest = mockk()
    private val portableIdentityCard: PortableIdentityCard = mockk()
    private val responseAudience = "testEndpointToSendIssuanceRequest"
    private val presentationRequest: PresentationRequest = mockk()
    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"

    @Test
    fun `test to create Issuance Response`() {
        every { issuanceRequest.contract.input.credentialIssuer } returns responseAudience
        val issuanceResponse = cardManager.createIssuanceResponse(issuanceRequest)
        val actualAudience = issuanceResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to create Presentation Response`() {
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest)
        val actualAudience = presentationResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to save card`() {
        coEvery { cardRepository.insert(portableIdentityCard) } returns Unit
        runBlocking {
            val actualResult = cardManager.saveCard(portableIdentityCard)
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
        }
    }

    @Test
    fun `test send presentation response`() {
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest)
        every { presentationResponse.request.entityIdentifier } returns testEntityDid
        every { presentationResponse.request.entityName } returns testEntityName
        coEvery { cardRepository.sendPresentationResponse(any(), any(), any()) } returns Result.Success(Unit)

        runBlocking {
            val responder: Identifier = mockk()
            val presentationResult = cardManager.sendPresentationResponse(presentationResponse, responder)
            assertThat(presentationResult).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            cardManager.createPresentationResponse(presentationRequest)
            cardManager.sendPresentationResponse(any(), any(), any())
            cardRepository.sendPresentationResponse(any(), any(), any())
            cardManager["createAndSaveReceipt"](allAny<PresentationResponse>())
            presentationResponse.createReceiptsForPresentedCredentials(testEntityDid, testEntityName)
        }
        presentationResponse.getCollectedCards()?.size?.let {
            coVerify(atMost = it) {
                cardManager["saveReceipt"](allAny<Receipt>())
            }
        }
        confirmVerified(cardManager)
    }
}