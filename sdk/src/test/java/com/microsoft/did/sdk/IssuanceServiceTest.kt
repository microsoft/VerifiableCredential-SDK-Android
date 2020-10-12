// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.assertj.core.api.Assertions
import org.junit.Test

/*
class IssuanceServiceTest {
        private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val revocationManager = RevocationManager(verifiableCredentialHolderRepository, receiptRepository)
    private val resolver: Resolver = mockk()
    private val cardManager =
        spyk(
            VerifiableCredentialManager(
                verifiableCredentialHolderRepository,
                receiptRepository,
                serializer,
                presentationRequestValidator,
                revocationManager,
                resolver
            )
        )
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
    fun `test to create Issuance Response`() {
        every { issuanceRequest.contract.input.credentialIssuer } returns responseAudience
        val issuanceResponse = cardManager.createIssuanceResponse(issuanceRequest, mockedPairwiseId)
        val actualAudience = issuanceResponse.audience
        val expectedAudience = responseAudience
        Assertions.assertThat(actualAudience).isEqualTo(expectedAudience)
    }
}*/
