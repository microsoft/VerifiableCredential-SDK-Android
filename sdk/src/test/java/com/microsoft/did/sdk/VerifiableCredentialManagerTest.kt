// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.entities.receipts.Receipt
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialRepository
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiableCredentialManagerTest {
    private val verifiableCredentialRepository: VerifiableCredentialRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val cardManager = spyk(VerifiableCredentialManager(verifiableCredentialRepository, serializer, presentationRequestValidator))
    private val issuanceRequest: IssuanceRequest = mockk()
    private val verifiableCredentialHolder: VerifiableCredentialHolder = mockk()
    private val responseAudience = "testEndpointToSendIssuanceRequest"
    private val presentationRequest: PresentationRequest = mockk()
    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"
    private val mockedPairwiseId: Identifier = mockk()

    @Test
    fun `test to create Issuance Response`() {
        every { issuanceRequest.contract.input.credentialIssuer } returns responseAudience
        val issuanceResponse = cardManager.createIssuanceResponse(issuanceRequest, mockedPairwiseId)
        val actualAudience = issuanceResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to create Presentation Response`() {
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest, mockedPairwiseId)
        val actualAudience = presentationResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to save card`() {
        coEvery { verifiableCredentialRepository.insert(verifiableCredentialHolder) } returns Unit
        runBlocking {
            val actualResult = cardManager.saveVch(verifiableCredentialHolder)
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
        }
    }

    @Test
    fun `test send presentation response`() {
        val responder: Identifier = mockk()
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest, responder)
        every { presentationResponse.request.entityIdentifier } returns testEntityDid
        every { presentationResponse.request.entityName } returns testEntityName
        coEvery { verifiableCredentialRepository.sendPresentationResponse(any(), any(), any()) } returns Result.Success(Unit)

        runBlocking {
            val presentationResult = cardManager.sendPresentationResponse(presentationResponse)
            assertThat(presentationResult).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            cardManager.createPresentationResponse(presentationRequest, responder)
            cardManager.sendPresentationResponse(any(), any())
            verifiableCredentialRepository.sendPresentationResponse(any(), any(), any())
            presentationResponse.createReceiptsForPresentedVerifiableCredentials(testEntityDid, testEntityName)
        }
        presentationResponse.requestedVchPresentationSubmissionMap.size.let {
            coVerify(exactly = it) {
                verifiableCredentialRepository.insert(any<com.microsoft.did.entities.receipts.Receipt>())
            }
        }
    }
}