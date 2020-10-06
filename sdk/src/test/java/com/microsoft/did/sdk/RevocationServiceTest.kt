// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class RevocationServiceTest {

    private val revocationService = RevocationService(mockk(), mockk(), mockk(), mockk())

    private val revocationReceipt: RevocationReceipt = mockk()
    private val revokedRPs = arrayOf("did:ion:test")
    private val vcId = "testCardId"

    @Test
    fun `test revoke verifiable presentation successfully`() {
        val revokeRPMap = mapOf("did:ion:test" to "test.com")
        val revokeReason = "testing revoke"

        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns vcId
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any()) }

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, revokeRPMap, revokeReason)
            Assertions.assertThat(status).isInstanceOf(Result.Success::class.java)
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
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns vcId
        val revokeRPMap = mapOf("did:ion:test" to "test.com")
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any()) }

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, revokeRPMap, "")
            Assertions.assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, revokeRPMap.keys.toList(), "")
        }
    }

    @Test
    fun `test revoke verifiable presentation for all RPs`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns vcId
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any()) }

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, emptyMap(), "")
            Assertions.assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, emptyList(), "")
        }
    }

    @Test
    fun `test revoke verifiable presentation no card Id`() {
        coEvery { verifiableCredentialHolderRepository.revokeVerifiablePresentation(any(), any(), any()) } returns Result.Success(
            revocationReceipt
        )
        every { revocationReceipt.relyingPartyList } returns revokedRPs
        every { verifiableCredentialHolder.cardId } returns ""
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any()) }

        runBlocking {
            val status = cardManager.revokeSelectiveOrAllVerifiablePresentation(verifiableCredentialHolder, emptyMap(), "")
            Assertions.assertThat(status).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            verifiableCredentialHolderRepository.revokeVerifiablePresentation(verifiableCredentialHolder, emptyList(), "")
        }
    }
}