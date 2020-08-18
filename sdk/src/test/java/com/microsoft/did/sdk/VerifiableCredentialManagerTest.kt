// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
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
    private val verifiableCredentialHolderRepository: VerifiableCredentialHolderRepository = mockk()
    private val receiptRepository: ReceiptRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val cardManager =
        spyk(VerifiableCredentialManager(verifiableCredentialHolderRepository, receiptRepository, serializer, presentationRequestValidator))
    private val issuanceRequest: IssuanceRequest
    private val verifiableCredentialHolder: VerifiableCredentialHolder = mockk()
    private val attestations: CredentialAttestations = mockk()
    private val responseAudience = "testEndpointToSendIssuanceRequest"
    private val presentationRequest: PresentationRequest = mockk()
    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"
    private val revocationReceipt: RevocationReceipt = mockk()
    private val revokedRPs = arrayOf("did:ion:test")
    private val verifiableCredentialHolderCardId = "testCardId"
    private var issuanceResponse: IssuanceResponse
    private var vcContract: VerifiableCredentialContract = mockk()
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        issuanceRequest = IssuanceRequest(vcContract, "testContractUrl")
        every { issuanceRequest.contract.input.credentialIssuer } returns credentialIssuer
        issuanceResponse = IssuanceResponse(issuanceRequest)
    }

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
        coEvery { verifiableCredentialHolderRepository.insert(verifiableCredentialHolder) } returns Unit
        runBlocking {
            val actualResult = cardManager.saveVch(verifiableCredentialHolder)
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
        }
    }

    @Test
    fun `test send presentation response`() {
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest)
        every { presentationResponse.request.entityIdentifier } returns testEntityDid
        every { presentationResponse.request.entityName } returns testEntityName
        coEvery { verifiableCredentialHolderRepository.sendPresentationResponse(any(), any(), any(), any()) } returns Result.Success(Unit)
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any())}

        runBlocking {
            val responder: Identifier = mockk()
            val presentationResult = cardManager.sendPresentationResponse(presentationResponse, responder)
            assertThat(presentationResult).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            cardManager.createPresentationResponse(presentationRequest)
            cardManager.sendPresentationResponse(any(), any(), any())
            verifiableCredentialHolderRepository.sendPresentationResponse(any(), any(), any(), any())
            receiptRepository.createAndSaveReceiptsForVCs(
                testEntityDid,
                testEntityName,
                ReceiptAction.Presentation,
                issuanceResponse.getRequestedVchs().values.map { it.cardId }
            )
        }
        presentationResponse.getRequestedVchs()?.size?.let {
            coVerify(exactly = it) {
                receiptRepository.insert(any())
            }
        }
    }

    @Test
    fun `test revoke verifiable presentation successfully`() {
        val revokeRPMap = mapOf("did:ion:test" to "test.com")
        val revokeReason = "testing revoke"

        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(revocationReceipt)
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns verifiableCredentialHolderCardId
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any())}

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, revokeRPMap, revokeReason)
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(
                verifiableCredentialHolder,
                revokeRPMap.keys.toList(),
                revokeReason
            )
        }
    }

    @Test
    fun `test revoke verifiable presentation no reason`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(revocationReceipt)
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns verifiableCredentialHolderCardId
        val revokeRPMap = mapOf("did:ion:test" to "test.com")
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any())}

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, revokeRPMap, "")
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, revokeRPMap.keys.toList(), "")
        }
    }

    @Test
    fun `test revoke verifiable presentation for all RPs`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(revocationReceipt)
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns verifiableCredentialHolderCardId
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any())}

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, emptyMap(), "")
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, emptyList(), "")
        }
    }

    @Test
    fun `test revoke verifiable presentation no card Id`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(revocationReceipt)
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns ""
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any())}

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, emptyMap(), "")
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, emptyList(), "")
        }
    }
}