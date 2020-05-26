// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.microsoft.portableIdentity.sdk.auth.models.attestations.CredentialAttestations
import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Test

class ResponseTest {
    var response: IssuanceResponse
    var request: IssuanceRequest
    @MockK
    val attestations: CredentialAttestations = mockk()
    @MockK
    var picContract: PicContract = mockk()
    private val entityName = "testEntityName"
    private val entityDid = "testEntityDid"
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"
    private val cardId = "testCardId"

    init {
        every { picContract.input.attestations } returns attestations
        every { picContract.display.card.issuedBy } returns issuedBy
        every { picContract.input.issuer } returns issuer
        request = IssuanceRequest(picContract, "testContractUrl")
        every { request.contract.input.credentialIssuer } returns credentialIssuer
        response = IssuanceResponse(request)
        val piCard: PortableIdentityCard = mockk()
        response.addCard(piCard, "testCard1")
        every { piCard.cardId } returns cardId
    }

    @Test
    fun `test add and get card`() {
        val suppliedPortableIdentityCard: PortableIdentityCard = mockk()
        val suppliedCardType = "testCard2"
        response.addCard(suppliedPortableIdentityCard, suppliedCardType)
        val actualCollectedCards = response.getCollectedCards()
        val expectedCardCount = 2
        assertThat(actualCollectedCards).isNotNull()
        assertThat(actualCollectedCards?.size).isEqualTo(expectedCardCount)
        assertThat(actualCollectedCards?.get(suppliedCardType)).isNotNull()
    }

    @Test
    fun `test add and get Id Tokens`() {
        val suppliedIdToken = "testIdToken"
        val suppliedIdTokenConfiguration = "testIdTokenConfig"
        response.addIdToken(suppliedIdTokenConfiguration, suppliedIdToken)
        val actualCollectedTokens = response.getCollectedIdTokens()
        val expectedTokenCount = 1
        assertThat(actualCollectedTokens).isNotNull()
        assertThat(actualCollectedTokens?.size).isEqualTo(expectedTokenCount)
        assertThat(actualCollectedTokens?.get(suppliedIdTokenConfiguration)).isNotNull()
    }

    @Test
    fun `test add and get Self Issued Claims`() {
        val suppliedSelfIssuedClaim = "testSelfIssuedClaim"
        val suppliedSelfIssuedClaimField = "testSelfIssuedClaimField"
        response.addSelfIssuedClaim(suppliedSelfIssuedClaimField, suppliedSelfIssuedClaim)
        val actualSelfIssuedClaims = response.getCollectedSelfIssuedClaims()
        val expectedSelfIssuedClaimCount = 1
        assertThat(actualSelfIssuedClaims).isNotNull()
        assertThat(actualSelfIssuedClaims?.size).isEqualTo(expectedSelfIssuedClaimCount)
        assertThat(actualSelfIssuedClaims?.get(suppliedSelfIssuedClaimField)).isNotNull()
    }

    @Test
    fun `test create receipt`() {
        val receipts = response.createReceiptsForPresentedCredentials(entityDid, entityName)
        val expectedReceiptCount = 1
        assertThat(receipts.size).isEqualTo(expectedReceiptCount)
        val receipt = receipts.first()
        assertThat(receipt.cardId).isEqualTo(cardId)
        assertThat(receipt.entityName).isEqualTo(entityName)
        assertThat(receipt.entityIdentifier).isEqualTo(entityDid)
    }
}