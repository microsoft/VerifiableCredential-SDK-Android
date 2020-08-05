// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.responses

import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResponseTest {
    private var response: IssuanceResponse
    private var request: IssuanceRequest
    private val attestations: CredentialAttestations = mockk()
    private var vcContract: VerifiableCredentialContract = mockk()
    private val entityName = "testEntityName"
    private val entityDid = "testEntityDid"
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"
    private val expectedType1 = "type2354"
    private val expectedType2 = "type4009"

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        request = IssuanceRequest(vcContract, "testContractUrl", vcContract.input.attestations)
        every { request.contract.input.credentialIssuer } returns credentialIssuer
        response = IssuanceResponse(request)
    }

    @Test
    fun `test add and get card`() {
        val suppliedVerifiableCredentialHolder1: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        val suppliedCardType1 = "testCard1"
        every { suppliedPresentationAttestation1.credentialType } returns suppliedCardType1
        response.addRequestedVch(suppliedPresentationAttestation1, suppliedVerifiableCredentialHolder1)
        val suppliedVerifiableCredentialHolder2: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation2: PresentationAttestation = mockk()
        val suppliedCardType2 = "testCard2"
        every { suppliedPresentationAttestation2.credentialType } returns suppliedCardType2
        response.addRequestedVch(suppliedPresentationAttestation2, suppliedVerifiableCredentialHolder2)
        val actualCollectedCards = response.getRequestedVchs()
        val expectedCardCount = 2
        assertThat(actualCollectedCards).isNotNull
        assertThat(actualCollectedCards.size).isEqualTo(expectedCardCount)
        assertThat(actualCollectedCards[suppliedPresentationAttestation1]).isEqualTo(suppliedVerifiableCredentialHolder1)
        assertThat(actualCollectedCards[suppliedPresentationAttestation2]).isEqualTo(suppliedVerifiableCredentialHolder2)
    }

    @Test
    fun `test add and get Id Tokens`() {
        val suppliedIdTokenAttestation: IdTokenAttestation = mockk()
        val suppliedIdTokenConfiguration = "testIdTokenConfig253"
        every { suppliedIdTokenAttestation.configuration } returns suppliedIdTokenConfiguration
        val suppliedIdToken = "testIdToken423"
        response.addRequestedIdToken(suppliedIdTokenAttestation, suppliedIdToken)
        val actualCollectedTokens = response.getRequestedIdTokens()
        val expectedTokenCount = 1
        assertThat(actualCollectedTokens).isNotNull
        assertThat(actualCollectedTokens.size).isEqualTo(expectedTokenCount)
        assertThat(actualCollectedTokens[suppliedIdTokenConfiguration]).isEqualTo(suppliedIdToken)
    }

    @Test
    fun `test add and get Self Issued Claims`() {
        val suppliedSelfIssuedClaim = "testSelfIssuedClaim"
        val suppliedSelfIssuedClaimField = "testSelfIssuedClaimField"
        response.addRequestedSelfAttestedClaim(suppliedSelfIssuedClaimField, suppliedSelfIssuedClaim)
        val actualSelfIssuedClaims = response.getRequestedSelfAttestedClaims()
        val expectedSelfIssuedClaimCount = 1
        assertThat(actualSelfIssuedClaims).isNotNull
        assertThat(actualSelfIssuedClaims.size).isEqualTo(expectedSelfIssuedClaimCount)
        assertThat(actualSelfIssuedClaims[suppliedSelfIssuedClaimField]).isEqualTo(suppliedSelfIssuedClaim)
    }

    @Test
    fun `test create receipt by adding empty card id`() {
        val vch: VerifiableCredentialHolder = mockk()
        val presentationAttestation: PresentationAttestation = mockk()
        every { presentationAttestation.credentialType } returns expectedType1
        val receiptCreationStartTime = System.currentTimeMillis()
        response.addRequestedVch(presentationAttestation, vch)
        val cardId = ""
        every { vch.cardId } returns cardId
        val receipts = response.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
        val expectedReceiptCount = 1
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
        val receipt = receipts.first()
        assertThat(receipt.vcId).isEqualTo("")
        assertThat(receipt.entityName).isEqualTo(entityName)
        assertThat(receipt.entityIdentifier).isEqualTo(entityDid)
        assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
        assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
    }

    @Test
    fun `test create receipt by adding 1 card`() {
        val vch: VerifiableCredentialHolder = mockk()
        val presentationAttestation: PresentationAttestation = mockk()
        every { presentationAttestation.credentialType } returns expectedType1
        val receiptCreationStartTime = System.currentTimeMillis()
        response.addRequestedVch(presentationAttestation, vch)
        val cardId = "testCardId"
        every { vch.cardId } returns cardId
        val receipts = response.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
        val expectedReceiptCount = 1
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
        val receipt = receipts.first()
        assertThat(receipt.vcId).isEqualTo(cardId)
        assertThat(receipt.entityName).isEqualTo(entityName)
        assertThat(receipt.entityIdentifier).isEqualTo(entityDid)
        assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
        assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
    }

    @Test
    fun `test create receipt without adding card`() {
        val receipts = response.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
        val expectedReceiptCount = 0
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
    }

    @Test
    fun `test create receipt by adding multiple cards with same type`() {
        val vch1: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation1.credentialType } returns expectedType1
        response.addRequestedVch(suppliedPresentationAttestation1, vch1)
        val vchId1 = "vchId1"
        every { vch1.cardId } returns vchId1
        val vch2: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation2: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation2.credentialType } returns expectedType1
        response.addRequestedVch(suppliedPresentationAttestation2, vch2)
        val vchId2 = "vchId2"
        every { vch2.cardId } returns vchId2
        val receiptCreationStartTime = System.currentTimeMillis()
        val receipts = response.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
        val expectedReceiptCount = 2
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
        val receipt = receipts.first()
        assertThat(receipt.vcId).isEqualTo(vchId1)
        assertThat(receipt.entityName).isEqualTo(entityName)
        assertThat(receipt.entityIdentifier).isEqualTo(entityDid)
        assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
        assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
    }

    @Test
    fun `test create receipt by adding multiple cards with different types`() {
        val vch1: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation1.credentialType } returns expectedType1
        response.addRequestedVch(suppliedPresentationAttestation1, vch1)
        val cardId1 = "testCardId1"
        every { vch1.cardId } returns cardId1
        val vch2: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation2: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation2.credentialType } returns expectedType2
        response.addRequestedVch(suppliedPresentationAttestation2, vch2)
        val cardId2 = "testCardId2"
        every { vch2.cardId } returns cardId2
        val receiptCreationStartTime = System.currentTimeMillis()
        val receipts = response.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
        val expectedReceiptCount = 2
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
        val receipt1 = receipts.first()
        assertThat(receipt1.vcId).isEqualTo(cardId1)
        assertThat(receipt1.entityName).isEqualTo(entityName)
        assertThat(receipt1.entityIdentifier).isEqualTo(entityDid)
        assertThat(receipt1.action).isEqualTo(ReceiptAction.Presentation)
        val receipt2 = receipts.last()
        assertThat(receipt2.vcId).isEqualTo(cardId2)
        assertThat(receipt2.entityName).isEqualTo(entityName)
        assertThat(receipt2.entityIdentifier).isEqualTo(entityDid)
        assertThat(receipt2.action).isEqualTo(ReceiptAction.Presentation)
        assertThat(receipt1.activityDate).isLessThanOrEqualTo(receipt2.activityDate)
        assertThat(receipt1.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
    }

    @Test
    fun `test create receipt by adding empty entity information`() {
        val vch: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        every { suppliedPresentationAttestation1.credentialType } returns expectedType1
        response.addRequestedVch(suppliedPresentationAttestation1, vch)
        val cardId = "testCardId"
        every { vch.cardId } returns cardId
        val receiptCreationStartTime = System.currentTimeMillis()
        val receipts = response.createReceiptsForPresentedVerifiableCredentials("", "")
        val expectedReceiptCount = 1
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
        val receipt = receipts.first()
        assertThat(receipt.vcId).isEqualTo(cardId)
        assertThat(receipt.entityName).isEqualTo("")
        assertThat(receipt.entityIdentifier).isEqualTo("")
        assertThat(receipt.action).isEqualTo(ReceiptAction.Presentation)
        assertThat(receipt.activityDate).isGreaterThanOrEqualTo(receiptCreationStartTime)
    }
}