// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.util.serializer.Serializer
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiableCredentialManagerTest {
    private val verifiableCredentialHolderRepository: VerifiableCredentialHolderRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val cardManager =
        spyk(VerifiableCredentialManager(verifiableCredentialHolderRepository, serializer, presentationRequestValidator))
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

        runBlocking {
            val responder: Identifier = mockk()
            val presentationResult = cardManager.sendPresentationResponse(presentationResponse, responder)
            assertThat(presentationResult).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            cardManager.createPresentationResponse(presentationRequest)
            cardManager.sendPresentationResponse(any(), any(), any())
            verifiableCredentialHolderRepository.sendPresentationResponse(any(), any(), any(), any())
            cardManager.createAndSaveReceipts(testEntityDid,
                testEntityName,
                ReceiptAction.Presentation,
                issuanceResponse.getRequestedVchs().values.map { it.cardId })
        }
        presentationResponse.getRequestedVchs()?.size?.let {
            coVerify(exactly = it) {
                verifiableCredentialHolderRepository.insert(any<Receipt>())
            }
        }
    }

    @Test
    fun `test revoke verifiable presentation successfully`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns verifiableCredentialHolderCardId
        val revokeRPMap = mapOf("test.com" to "did:ion:test")
        val revokeReason = "testing revoke"

        runBlocking {
            val status = cardManager.revokeVerifiablePresentation(verifiableCredentialHolder, revokeRPMap, revokeReason)
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(
                verifiableCredentialHolder,
                revokeRPMap.values.toList(),
                revokeReason
            )
            verifiableCredentialHolderRepository.insert(any<Receipt>())
        }
    }

    @Test
    fun `test revoke verifiable presentation no reason`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns verifiableCredentialHolderCardId
        val revokeRPMap = mapOf("test.com" to "did:ion:test")

        runBlocking {
            val status = cardManager.revokeVerifiablePresentation(verifiableCredentialHolder, revokeRPMap, "")
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, revokeRPMap.values.toList(), "")
            verifiableCredentialHolderRepository.insert(any<Receipt>())
        }
    }

    @Test
    fun `test revoke verifiable presentation no RP`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns verifiableCredentialHolderCardId

        runBlocking {
            val status = cardManager.revokeVerifiablePresentation(verifiableCredentialHolder, null, "")
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, null, "")
            verifiableCredentialHolderRepository.insert(any<Receipt>())
        }
    }

    @Test
    fun `test revoke verifiable presentation no card Id`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns ""

        runBlocking {
            val status = cardManager.revokeVerifiablePresentation(verifiableCredentialHolder, null, "")
            assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, null, "")
            verifiableCredentialHolderRepository.insert(any<Receipt>())
        }
    }
}