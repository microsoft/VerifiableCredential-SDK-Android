// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.responses

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResponseTest {
    private var response: IssuanceResponse
    private var request: IssuanceRequest
    private val attestations: CredentialAttestations = mockk()
    private var vcContract: VerifiableCredentialContract = mockk()
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        request = IssuanceRequest(vcContract, "testContractUrl")
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
}