// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.validators.PresentationRequestValidator
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.CardRepository
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardManagerTest {
    private val cardRepository: CardRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val cardManager = CardManager(cardRepository, serializer, presentationRequestValidator)
    private val issuanceRequest: IssuanceRequest = mockk()
    private val responder: Identifier = mockk()
    private val verifiableCredential: VerifiableCredential = mockk()
    private val portableIdentityCard: PortableIdentityCard = mockk()
    private val responseAudience = "testEndpointToSendIssuanceRequest"
    private val presentationRequest: PresentationRequest = mockk()
    private val presentationResponse: PresentationResponse
    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"

    init {
        every { runBlocking { cardRepository.sendIssuanceResponse(any(), responder) } } returns Result.Success(verifiableCredential)
        every { runBlocking { cardRepository.insert(verifiableCredential) } } returns Unit
        every { runBlocking { cardRepository.insert(portableIdentityCard) } } returns Unit
        every { runBlocking { cardRepository.sendPresentationResponse(any(), any(), any()) } } returns Result.Success(Unit)
        every { issuanceRequest.contract.input.credentialIssuer } returns responseAudience
        every { presentationRequest.content.redirectUrl } returns responseAudience
        presentationResponse = cardManager.createPresentationResponse(presentationRequest)
        every { presentationResponse.request.entityIdentifier } returns testEntityDid
        every { presentationResponse.request.entityName } returns testEntityName
    }

    @Test
    fun `test to create Issuance Response`() {
        val issuanceResponse = cardManager.createIssuanceResponse(issuanceRequest)
        val actualAudience = issuanceResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to create Presentation Response`() {
        val actualAudience = presentationResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to save card`() {
        runBlocking {
            val actualResult = cardManager.saveCard(portableIdentityCard)
            assertThat(actualResult is Result.Success).isTrue()
        }
    }

    @Test
    fun `test send presentation response`() {
        runBlocking {
            val presentationResponse = cardManager.createPresentationResponse(presentationRequest)
            val responder: Identifier = mockk()
            val presentationResult = cardManager.sendPresentationResponse(presentationResponse, responder)
            assertThat(presentationResult is Result.Success).isTrue()
        }
    }
}