// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.responses

import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResponseTest {
    private val issuanceResponse: IssuanceResponse
    private val issuanceRequest: IssuanceRequest
    private val presentationResponse: PresentationResponse
    private val presentationRequest: PresentationRequest
    private val attestations: CredentialAttestations = mockk()
    private var vcContract: VerifiableCredentialContract = mockk()
    private val mockedPairwiseId: Identifier = mockk()
    private val entityName = "testEntityName"
    private val entityDid = "testEntityDid"
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"
    private val mockedPairwiseDid = "testDid"
    private val suppliedRequestToken =
        """eyJ0eXAiOiJKV1QiLCJraWQiOiJkaWQ6aW9uOkVpQURxQzdCcUw5endmdnhNQmJQUy1yc1dNa1ZKMG11RnhPbGNxSWwxTFA4eUE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1NrRnRZalZxVUhRME0xOHdPR3RzU0doc01EWTViVTFTZEZoV1kxQTRRbHBwYjBKb1gwNVNZbEkyZHlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVFek4yRnZkbEpzYVROMlZsSkJhVnBYWVhWVWFtMWxkVkpqWlRVM1YyWlRRUzFoYlhoc1dHVlJSWEIzSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUVRoeVpIVk1ZM2xHV1hOUU1HVjJaR1ZJTVRWWFEwOURUSEIyUWkxTVVHWldWbmQzV1RacFgwVXlXVkVpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5ibWx1WnlJc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpTENKcWQyc2lPbnNpYTNSNUlqb2lSVU1pTENKamNuWWlPaUp6WldOd01qVTJhekVpTENKNElqb2liVVJqVFY5TVR6QnZWVXQ0UTNaM1lqRTNSbGxUZW5aaVVHSkNTVWhtUjJSSWNUVmtlbk5aYjAxWFl5SXNJbmtpT2lKSWFVRm5kbUZuYmswMFYxZFdRVU16UVhGU2RsaGpVV3RGTFcxV09UVklUV0ZIWVVWbWNVZG5VRmc0SW4wc0luQjFjbkJ2YzJVaU9sc2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEjc2lnbmluZyIsImFsZyI6IkVTMjU2SyJ9.eyJyZXNwb25zZV90eXBlIjoiaWR0b2tlbiIsInJlc3BvbnNlX21vZGUiOiJmb3JtX3Bvc3QiLCJjbGllbnRfaWQiOiJjbGllbnRJZCIsInJlZGlyZWN0X3VyaSI6InJlZGlyZWN0VXJpIiwic2NvcGUiOiJvcGVuaWQgZGlkX2F1dGhuIiwiaXNzIjoiZGlkOmlvbjpFaUFEcUM3QnFMOXp3ZnZ4TUJiUFMtcnNXTWtWSjBtdUZ4T2xjcUlsMUxQOHlBPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbENTa0Z0WWpWcVVIUTBNMTh3T0d0c1NHaHNNRFk1YlUxU2RGaFdZMUE0UWxwcGIwSm9YMDVTWWxJMmR5SXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVRXpOMkZ2ZGxKc2FUTjJWbEpCYVZwWFlYVlVhbTFsZFZKalpUVTNWMlpUUVMxaGJYaHNXR1ZSUlhCM0luMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFFUaHlaSFZNWTNsR1dYTlFNR1YyWkdWSU1UVlhRMDlEVEhCMlFpMU1VR1pXVm5kM1dUWnBYMFV5V1ZFaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuYm1sdVp5SXNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl3TVRraUxDSnFkMnNpT25zaWEzUjVJam9pUlVNaUxDSmpjbllpT2lKelpXTndNalUyYXpFaUxDSjRJam9pYlVSalRWOU1UekJ2VlV0NFEzWjNZakUzUmxsVGVuWmlVR0pDU1VobVIyUkljVFZrZW5OWmIwMVhZeUlzSW5raU9pSklhVUZuZG1GbmJrMDBWMWRXUVVNelFYRlNkbGhqVVd0RkxXMVdPVFZJVFdGSFlVVm1jVWRuVUZnNEluMHNJbkIxY25CdmMyVWlPbHNpWVhWMGFDSXNJbWRsYm1WeVlXd2lYWDFkZlgxZGZRIiwicmVnaXN0cmF0aW9uIjp7ImNsaWVudF9uYW1lIjoiY2xpZW50TmFtZSIsImNsaWVudF9wdXJwb3NlIjoiY2xpZW50UHVycG9zZSIsInRvc191cmkiOiJ0b3NVcmkiLCJsb2dvX3VyaSI6ImxvZ29VcmkifSwiaWF0IjoxNTk2NjEyMjk3LCJleHAiOjE1OTcyMTcwOTcsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6ImlucHV0RGVzY3JpcHRvcklkIiwic2NoZW1hIjp7InVybCI6WyJodHRwczovL3NjaGVtYS5leGFtcGxlLmNvbS9kcml2aW5nbGljZW5zZSJdLCJuYW1lIjoic2NoZW1hTmFtZSIsInB1cnBvc2UiOiJzY2hlbWFQdXJwb3NlIn0sImlzc3VhbmNlIjpbeyJkaWQiOiJkaWQ6dXNlciIsIm1hbmlmZXN0IjoiaHR0cHM6Ly9jb250cmFjdC5leGFtcGxlLmNvbSJ9XX1dLCJuYW1lIjoicHJlc2VudGF0aW9uRGVmaW5pdGlvbk5hbWUiLCJwdXJwb3NlIjoicHJlc2VudGF0aW9uRGVmaW5pdGlvblB1cnBvc2UifX0.yzo2VdVGI6tCKKsHjM9jJJ_nbtX8fWgpx0b2jkZtNyOJ1gsallk-C8kCnkMzwlfr0QPle63nUaTTK5m9lG7hNQ"""

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        issuanceRequest = IssuanceRequest(vcContract, "testContractUrl")
        every { issuanceRequest.contract.input.credentialIssuer } returns credentialIssuer
        issuanceResponse = IssuanceResponse(issuanceRequest, mockedPairwiseId)
        every { issuanceResponse.responder.id } returns mockedPairwiseDid
        val serializer = Serializer()
        val oidcRequestContent =
            serializer.parse(PresentationRequestContent.serializer(), JwsToken.deserialize(suppliedRequestToken, serializer).content())
        presentationRequest = PresentationRequest(suppliedRequestToken, oidcRequestContent)
        presentationResponse = PresentationResponse(presentationRequest, mockedPairwiseId)
    }

    @Test
    fun `test add and get card`() {
        val suppliedVerifiableCredentialHolder1: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation1: PresentationAttestation = mockk()
        val suppliedCardType1 = "testCard1"
        every { suppliedPresentationAttestation1.credentialType } returns suppliedCardType1
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation1] = suppliedVerifiableCredentialHolder1
        val suppliedVerifiableCredentialHolder2: VerifiableCredentialHolder = mockk()
        val suppliedPresentationAttestation2: PresentationAttestation = mockk()
        val suppliedCardType2 = "testCard2"
        every { suppliedPresentationAttestation2.credentialType } returns suppliedCardType2
        issuanceResponse.requestedVchMap[suppliedPresentationAttestation2] = suppliedVerifiableCredentialHolder2
        val actualCollectedCards = issuanceResponse.requestedVchMap
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
        issuanceResponse.requestedIdTokenMap[suppliedIdTokenAttestation.configuration] = suppliedIdToken
        val actualCollectedTokens = issuanceResponse.requestedIdTokenMap
        val expectedTokenCount = 1
        assertThat(actualCollectedTokens).isNotNull
        assertThat(actualCollectedTokens.size).isEqualTo(expectedTokenCount)
        assertThat(actualCollectedTokens[suppliedIdTokenConfiguration]).isEqualTo(suppliedIdToken)
    }

    @Test
    fun `test add and get Self Issued Claims`() {
        val suppliedSelfIssuedClaim = "testSelfIssuedClaim"
        val suppliedSelfIssuedClaimField = "testSelfIssuedClaimField"
        issuanceResponse.requestedSelfAttestedClaimMap[suppliedSelfIssuedClaimField] = suppliedSelfIssuedClaim
        val actualSelfIssuedClaims = issuanceResponse.requestedSelfAttestedClaimMap
        val expectedSelfIssuedClaimCount = 1
        assertThat(actualSelfIssuedClaims).isNotNull
        assertThat(actualSelfIssuedClaims.size).isEqualTo(expectedSelfIssuedClaimCount)
        assertThat(actualSelfIssuedClaims[suppliedSelfIssuedClaimField]).isEqualTo(suppliedSelfIssuedClaim)
    }

    @Test
    fun `test create receipt by adding empty card id`() {
        val vch: VerifiableCredentialHolder = mockk()
        val receiptCreationStartTime = System.currentTimeMillis()
        val credentialPresentationInputDescriptorMock: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock] = vch
        val cardId = ""
        every { vch.cardId } returns cardId
        val receipts = presentationResponse.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
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
        val receiptCreationStartTime = System.currentTimeMillis()
        val credentialPresentationInputDescriptorMock: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock] = vch
        val cardId = "testCardId"
        every { vch.cardId } returns cardId
        val receipts = presentationResponse.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
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
        val receipts = presentationResponse.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
        val expectedReceiptCount = 0
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
    }

    @Test
    fun `test create receipt by adding multiple cards with same type`() {
        val vch1: VerifiableCredentialHolder = mockk()
        val credentialPresentationInputDescriptorMock1: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock1] = vch1
        val vchId1 = "vchId1"
        every { vch1.cardId } returns vchId1
        val vch2: VerifiableCredentialHolder = mockk()
        val credentialPresentationInputDescriptorMock2: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock2] = vch2
        val vchId2 = "vchId2"
        every { vch2.cardId } returns vchId2
        val receiptCreationStartTime = System.currentTimeMillis()
        val receipts = presentationResponse.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
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
        val credentialPresentationInputDescriptorMock1: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock1] = vch1
        val cardId1 = "testCardId1"
        every { vch1.cardId } returns cardId1
        val vch2: VerifiableCredentialHolder = mockk()
        val credentialPresentationInputDescriptorMock2: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock2] = vch2
        val cardId2 = "testCardId2"
        every { vch2.cardId } returns cardId2
        val receiptCreationStartTime = System.currentTimeMillis()
        val receipts = presentationResponse.createReceiptsForPresentedVerifiableCredentials(entityDid, entityName)
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
        val credentialPresentationInputDescriptorMock: CredentialPresentationInputDescriptor = mockk()
        presentationResponse.requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptorMock] = vch
        val cardId = "testCardId"
        every { vch.cardId } returns cardId
        val receiptCreationStartTime = System.currentTimeMillis()
        val receipts = presentationResponse.createReceiptsForPresentedVerifiableCredentials("", "")
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