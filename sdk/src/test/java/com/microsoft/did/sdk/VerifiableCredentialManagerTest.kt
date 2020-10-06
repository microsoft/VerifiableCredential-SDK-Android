// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.entities.receipts.Receipt
import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.datasource.repository.ReceiptRepository
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiableCredentialManagerTest {
    private val verifiableCredentialRepository: VerifiableCredentialRepository = mockk()
    private val receiptRepository: ReceiptRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val revocationManager: RevocationManager = RevocationManager(verifiableCredentialHolderRepository, receiptRepository)
    private val cardManager = spyk(VerifiableCredentialManager(verifiableCredentialRepository, serializer, presentationRequestValidator))
    private val issuanceRequest: IssuanceRequest = mockk()
    private val verifiableCredentialHolder: VerifiableCredentialHolder = mockk()
    private val attestations: CredentialAttestations = mockk()
    private val responseAudience = "testEndpointToSendIssuanceRequest"
    private val presentationRequest: PresentationRequest = mockk()
    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"

    private var issuanceResponse: IssuanceResponse
    private var vcContract: VerifiableCredentialContract = mockk()
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"
    private val mockedPairwiseId: Identifier = mockk()

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        issuanceRequest = IssuanceRequest(vcContract, "testContractUrl")
        every { issuanceRequest.contract.input.credentialIssuer } returns credentialIssuer
        issuanceResponse = IssuanceResponse(issuanceRequest, mockedPairwiseId)
    }

    @Test
    fun `test send presentation response`() {
        val responder: Identifier = mockk()
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest, responder)
        every { presentationResponse.request.entityIdentifier } returns testEntityDid
        every { presentationResponse.request.entityName } returns testEntityName
        coEvery { verifiableCredentialRepository.sendPresentationResponse(any(), any(), any()) } returns Result.Success(Unit)
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any()) }

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
                receiptRepository.insert(any())
            }
        }
    }

}