// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.microsoft.did.sdk.credential.models.ServiceDescriptor
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.protectors.ExchangeResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ExchangeServiceTest {

    private val identifierManager: IdentifierManager = mockk()
    private val masterIdentifier: Identifier = mockk()
    private val pairwiseIdentifier: Identifier = mockk()

    private val mockedJwtValidator: JwtValidator = mockk()
    private val exchangeResponseFormatter: ExchangeResponseFormatter = mockk()
    private val exchangeService = ExchangeService(
        mockk(relaxed = true), exchangeResponseFormatter,
        defaultTestSerializer, mockedJwtValidator
    )
    private val suppliedVcJti = "testJti"
    private val suppliedVcRaw = "testVcRaw"
    private val suppliedVcSubject = "subject"
    private val suppliedVcIssuer = "Issuer"
    private val suppliedIssuedTime = 12345678L
    private val suppliedExpirationTime = 145678998L
    private val suppliedVcContent = VerifiableCredentialContent(
        suppliedVcJti,
        VerifiableCredentialDescriptor(
            listOf("contexts"),
            listOf("credentialTypes"),
            mapOf("credSubKey" to "credSubValue"),
            exchangeService = ServiceDescriptor("exchangeServiceId", "exchangeServiceType")
        ),
        suppliedVcSubject,
        suppliedVcIssuer,
        suppliedIssuedTime,
        suppliedExpirationTime
    )
    private val expectedVerifiableCredential =
        VerifiableCredential(
            suppliedVcJti,
            suppliedVcRaw,
            suppliedVcContent
        )
    private val suppliedExchangedVcJti = "testJti"
    private val suppliedExchangedVcRaw = "testVcRaw"
    private val suppliedExchangedVcSubject = "subject"
    private val suppliedExchangedVcIssuer = "Issuer"
    private val suppliedExchangedVcIssuedTime = 12345678L
    private val suppliedExchangedVcExpirationTime = 145678998L
    private val suppliedExchangedVcContent = VerifiableCredentialContent(
        suppliedExchangedVcJti,
        VerifiableCredentialDescriptor(listOf("contexts"), listOf("credentialTypes"), mapOf("credSubKey" to "credSubValue")),
        suppliedExchangedVcSubject,
        suppliedExchangedVcIssuer,
        suppliedExchangedVcIssuedTime,
        suppliedExchangedVcExpirationTime
    )
    private val expectedExchangedVerifiableCredential =
        VerifiableCredential(
            suppliedExchangedVcJti,
            suppliedExchangedVcRaw,
            suppliedExchangedVcContent
        )

    init {
        coEvery { identifierManager.getMasterIdentifier() } returns Result.Success(masterIdentifier)
        coEvery { identifierManager.getOrCreatePairwiseIdentifier(masterIdentifier, any()) } returns Result.Success(pairwiseIdentifier)
        mockkConstructor(SendVerifiableCredentialIssuanceRequestNetworkOperation::class)
    }

    @Test
    fun `test get exchanged verifiable credential`() {
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Success(
            expectedExchangedVerifiableCredential
        )
        every { pairwiseIdentifier.id } returns "testPairwiseDid"
        val exchangeRequest = ExchangeRequest(expectedVerifiableCredential, pairwiseIdentifier.id, masterIdentifier)
        every { exchangeResponseFormatter.formatResponse(exchangeRequest, 3600) } returns "testExchangeResponse"
        runBlocking {
            val actualExchangedVc =
                exchangeService.getExchangedVerifiableCredential(expectedVerifiableCredential, masterIdentifier, pairwiseIdentifier)
            assertThat(actualExchangedVc).isInstanceOf(Result.Success::class.java)
            assertThat((actualExchangedVc as Result.Success).payload).isEqualTo(expectedExchangedVerifiableCredential)
        }
    }
}