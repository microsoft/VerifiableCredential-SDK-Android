// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.RevocationRequest
import com.microsoft.did.sdk.credential.service.protectors.RevocationResponseFormatter
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiablePresentationRevocationRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class RevocationServiceTest {

    private val identifierService: IdentifierService = mockk()
    private val masterIdentifier: Identifier = mockk()
    private val revocationResponseFormatter: RevocationResponseFormatter = mockk()
    private val revocationService =
        spyk(RevocationService(mockk(relaxed = true), identifierService, revocationResponseFormatter, mockk()), recordPrivateCalls = true)

    private val revokeRpList = listOf("did:ion:test")
    private val revokeReason = "test reason"
    private val verifiableCredential: VerifiableCredential = mockk()
    private val formattedResponse = "FORMATTED_RESPONSE"

    @Before
    fun setup() {
        mockkConstructor(SendVerifiablePresentationRevocationRequestNetworkOperation::class)
        coEvery { identifierService.getMasterIdentifier() } returns Result.Success(masterIdentifier)
        coEvery { verifiableCredential.contents.vc.revokeService?.id } returns "https://microsoft.com/vcs"
        coEvery { revocationResponseFormatter.formatResponse(any(), any()) } returns formattedResponse
    }

    @Test
    fun `revoke verifiable presentation successfully with correct params`() {
        val expectedRevocationRequest = RevocationRequest(verifiableCredential, masterIdentifier, revokeRpList, revokeReason)
        val expectedRevocationReceipt: RevocationReceipt = mockk()
        coEvery { anyConstructed<SendVerifiablePresentationRevocationRequestNetworkOperation>().fire() } returns Result.Success(
            expectedRevocationReceipt
        )

        runBlocking {
            val actualResult = revocationService.revokeVerifiablePresentation(verifiableCredential, revokeRpList, revokeReason)
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertThat((actualResult as Result.Success).payload == expectedRevocationReceipt)
        }

        coVerify(exactly = 1) {
            revocationService["sendRevocationRequest"](expectedRevocationRequest, formattedResponse)
            anyConstructed<SendVerifiablePresentationRevocationRequestNetworkOperation>().fire()
        }
    }

    @Test
    fun `passing empty list results in failure`() {
        runBlocking {
            val actualResult = revocationService.revokeVerifiablePresentation(verifiableCredential, emptyList(), revokeReason)
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
        }
    }
}