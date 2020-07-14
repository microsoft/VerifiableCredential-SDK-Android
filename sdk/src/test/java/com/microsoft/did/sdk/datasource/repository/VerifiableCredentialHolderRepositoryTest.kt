/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.credential.models.*
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.models.requestMappings.VerifiableCredentialRequestMapping
import com.microsoft.did.sdk.credential.service.models.requestMappings.VerifiableCredentialHolderRequestMapping
import com.microsoft.did.sdk.credential.service.models.oidc.OidcRequestContent
import com.microsoft.did.sdk.credential.service.protectors.OidcResponseFormatter
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.ReceiptDao
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialDao
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialHolderDao
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendPresentationResponseNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import kotlinx.coroutines.runBlocking
import org.junit.Test
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.assertEquals

class VerifiableCredentialHolderRepositoryTest {

    private val database: SdkDatabase = mockk()
    private val mockedVcDao: VerifiableCredentialDao = mockk()
    private val mockedIssuanceResponse: IssuanceResponse = mockk()
    private val mockedIssuanceRequest: IssuanceRequest = mockk()
    private val mockedPresentationResponse: PresentationResponse = mockk()
    private val mockedPresentationRequest: PresentationRequest = mockk()
    private val mockedVcRequestMapping: VerifiableCredentialRequestMapping = mockk()
    private val mockedPrimeIdentifier: Identifier = mockk()
    private val mockedPairwiseIdentifier: Identifier = mockk()
    private val mockedFormatter: OidcResponseFormatter = mockk()
    private val mockedPrimeVcContent: VerifiableCredentialContent = mockk()
    private val mockedVpContext: VerifiableCredentialHolderRequestMapping = mockk()
    private val mockedVch: VerifiableCredentialHolder = mockk()
    private val mockedPrimeVc: VerifiableCredential = mockk()
    private val mockedExchangedVcContent: VerifiableCredentialContent = mockk()

    private val repository: VerifiableCredentialHolderRepository
    private val serializer: Serializer = Serializer()

    private val expectedAudience: String = "audience23430"
    private val expectedSignedResponseToken: String = "responseToken49235"
    private val expectedRpDid: String = "did:ion:rp53292"
    private val expectedPairwiseDid: String = "did:ion:pairwise238"
    private val expectedPrimeDid: String = "did:ion:prime98493"
    private val expectedContractUrl: String = "https://contract.com/2434"
    private val expectedIdTokenContextField: String = "idTokenField48239"
    private val expectedSelfAttestedClaimKey: String = "selfAttestedClaim3454"
    private val expectedIdTokenContextMapping = mutableMapOf(expectedIdTokenContextField to "")
    private val expectedSelfAttestedClaimContext = mutableMapOf(expectedSelfAttestedClaimKey to "")
    private val expectedVcToken: String = "vcToken523094"
    private val expectedPrimeVcJti: String = "primeJti23723"
    private val expectedExchangedVcJti: String = "exchangeJti293859"

    init {
        mockkConstructor(SendVerifiableCredentialIssuanceRequestNetworkOperation::class)
        mockkConstructor(SendPresentationResponseNetworkOperation::class)
        val apiProvider: ApiProvider = mockk()
        setUpFormatter()
        setUpDatabase()
        every { mockedPrimeIdentifier.id } returns expectedPrimeDid
        every { mockedPairwiseIdentifier.id } returns expectedPairwiseDid
        repository = VerifiableCredentialHolderRepository(database, apiProvider, mockedFormatter, serializer)
    }

    @Test
    fun `send issuance response successfully`() {
        setUpIssuanceResponse()
        setUpMockedVcContents(mockedPrimeVcContent, expectedPrimeVcJti, expectedPrimeDid)
        mockUnwrapSignedVcTopLevelFunction(mockedPrimeVcContent)
        val expectedVcRequestMapping = listOf(mockedVcRequestMapping)
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Success(expectedVcToken)

        runBlocking {
            val actualResult = repository.sendIssuanceResponse(
                mockedIssuanceResponse,
                expectedVcRequestMapping,
                mockedPrimeIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload.contents, mockedPrimeVcContent)
            assertEquals((actualResult).payload.jti, expectedPrimeVcJti)
            assertEquals((actualResult).payload.picId, expectedPrimeVcJti)
            assertEquals((actualResult).payload.raw, expectedVcToken)
        }
    }

    @Test
    fun `send issuance response with failed response from service`() {
        setUpIssuanceResponse()
        mockUnwrapSignedVcTopLevelFunction(mockedPrimeVcContent)
        val expectedVcRequestMapping = listOf(mockedVcRequestMapping)
        val expectedException = SdkException()
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Failure(
            expectedException
        )

        runBlocking {
            val actualResult = repository.sendIssuanceResponse(
                mockedIssuanceResponse,
                expectedVcRequestMapping,
                mockedPrimeIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertEquals((actualResult as Result.Failure).payload, expectedException)
        }
    }

    @Test
    fun `send presentation response successfully`() {
        setUpPresentationResponse()
        val expectedVcRequestMapping = listOf(mockedVcRequestMapping)
        coEvery { anyConstructed<SendPresentationResponseNetworkOperation>().fire() } returns Result.Success(Unit)

        runBlocking {
            val actualResult = repository.sendPresentationResponse(
                mockedPresentationResponse,
                expectedVcRequestMapping,
                mockedPrimeIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload, Unit)
        }
    }

    @Test
    fun `send presentation response with failed response from service`() {
        setUpPresentationResponse()
        val expectedVcRequestMapping = listOf(mockedVcRequestMapping)
        val expectedException = SdkException()
        coEvery { anyConstructed<SendPresentationResponseNetworkOperation>().fire() } returns Result.Failure(expectedException)

        runBlocking {
            val actualResult = repository.sendPresentationResponse(
                mockedPresentationResponse,
                expectedVcRequestMapping,
                mockedPrimeIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertEquals((actualResult as Result.Failure).payload, expectedException)
        }
    }

    @Test
    fun `send exchange response successfully`() {
        setUpMockedVcContents(mockedExchangedVcContent, expectedExchangedVcJti, expectedPairwiseDid)
        mockUnwrapSignedVcTopLevelFunction(mockedExchangedVcContent)
        setUpExchangeRequest()
        setUpVpContext()
        coEvery { repository.getAllVerifiableCredentialsById(expectedPrimeVcJti) } returns emptyList()
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Success(expectedVcToken)

        runBlocking {
            val actualResult = repository.getExchangedVerifiableCredential(
                mockedVpContext,
                mockedPairwiseIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload.raw, expectedVcToken)
            assertEquals((actualResult).payload.contents, mockedExchangedVcContent)
            assertEquals((actualResult).payload.jti, expectedExchangedVcJti)
            assertEquals((actualResult).payload.picId, expectedPrimeVcJti)
            assertEquals((actualResult).payload.raw, expectedVcToken)
        }
    }

    @Test
    fun `send exchange response with failed response from service`() {
        setUpMockedVcContents(mockedExchangedVcContent, expectedExchangedVcJti, expectedPairwiseDid)
        mockUnwrapSignedVcTopLevelFunction(mockedExchangedVcContent)
        setUpExchangeRequest()
        setUpVpContext()
        coEvery { repository.getAllVerifiableCredentialsById(expectedPrimeVcJti) } returns emptyList()
        val expectedException = SdkException()
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Failure(
            expectedException
        )

        runBlocking {
            val actualResult = repository.getExchangedVerifiableCredential(
                mockedVpContext,
                mockedPairwiseIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertEquals((actualResult as Result.Failure).payload, expectedException)
        }
    }

    @Test
    fun `get exchanged vc from database successfully`() {
        setUpVpContext()
        setUpMockedVcContents(mockedExchangedVcContent, expectedExchangedVcJti, expectedPairwiseDid)
        val mockedExchangedVc: VerifiableCredential = mockk()
        every { mockedExchangedVc.contents } returns mockedExchangedVcContent
        coEvery { repository.getAllVerifiableCredentialsById(expectedPrimeVcJti) } returns listOf(mockedExchangedVc)

        runBlocking {
            val actualResult = repository.getExchangedVerifiableCredential(
                mockedVpContext,
                mockedPairwiseIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload.contents, mockedExchangedVcContent)
        }
    }

    private fun setUpDatabase() {
        val mockedVchDao: VerifiableCredentialHolderDao = mockk()
        val mockedReceiptDao: ReceiptDao = mockk()
        every { database.verifiableCredentialHolderDao() } returns mockedVchDao
        every { database.receiptDao() } returns mockedReceiptDao
        every { database.verifiableCredentialDao() } returns mockedVcDao
        coEvery { mockedVcDao.insert(any()) } returns Unit
    }

    private fun setUpFormatter() {
        every {
            mockedFormatter.format(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns expectedSignedResponseToken
    }

    private fun setUpIssuanceResponse() {
        every { mockedIssuanceResponse.audience } returns expectedAudience
        every { mockedIssuanceResponse.request } returns mockedIssuanceRequest
        every { mockedIssuanceRequest.entityIdentifier } returns expectedRpDid
        every { mockedIssuanceRequest.contractUrl } returns expectedContractUrl
        every { mockedIssuanceResponse.getIdTokenRequestMapping() } returns expectedIdTokenContextMapping
        every { mockedIssuanceResponse.getSelfAttestedClaimRequestMapping() } returns expectedSelfAttestedClaimContext
    }

    private fun setUpPresentationResponse() {
        val oidcRequestContent: OidcRequestContent = mockk()
        every { mockedPresentationResponse.audience } returns expectedAudience
        every { mockedPresentationResponse.request } returns mockedPresentationRequest
        every { mockedPresentationRequest.entityIdentifier } returns expectedRpDid
        every { mockedPresentationRequest.content } returns oidcRequestContent
        every { oidcRequestContent.nonce } returns ""
        every { oidcRequestContent.state } returns ""
        every { mockedPresentationResponse.getIdTokenRequestMapping() } returns expectedIdTokenContextMapping
        every { mockedPresentationResponse.getSelfAttestedClaimRequestMapping() } returns expectedSelfAttestedClaimContext
    }

    private fun setUpExchangeRequest() {
        mockkConstructor(ExchangeRequest::class)
        every { anyConstructed<ExchangeRequest>().pairwiseDid } returns expectedPairwiseDid
        every { anyConstructed<ExchangeRequest>().verifiableCredential } returns mockedPrimeVc
        every { mockedPrimeVc.picId } returns expectedPrimeVcJti
        every { mockedPrimeVc.contents } returns mockedExchangedVcContent
    }

    private fun setUpVpContext() {
        every { mockedVpContext.verifiablePresentationHolder } returns mockedVch
        setUpMockedVch()
        setUpMockedPrimeVc()
        setUpMockedVcContents(mockedPrimeVcContent, expectedPrimeVcJti, expectedPrimeDid)
    }

    private fun setUpMockedVch() {
        every { mockedVch.owner } returns mockedPrimeIdentifier
        every { mockedVch.verifiableCredential } returns mockedPrimeVc
        every { mockedVch.cardId } returns expectedPrimeVcJti
    }

    private fun setUpMockedPrimeVc() {
        every { mockedPrimeVc.contents } returns mockedPrimeVcContent
        every { mockedPrimeVc.picId } returns expectedPrimeVcJti
    }

    private fun setUpMockedVcContents(vcContent: VerifiableCredentialContent, jti: String, subjectDid: String) {
        val mockedVcDescriptor: VerifiableCredentialDescriptor = mockk()
        val mockedServiceDescriptor: ServiceDescriptor = mockk()
        val expectedExchangeUrl = "https://exchange.com/23948"
        every { vcContent.vc } returns mockedVcDescriptor
        every { vcContent.jti } returns jti
        every { vcContent.sub } returns subjectDid
        every { mockedVcDescriptor.exchangeService } returns mockedServiceDescriptor
        every { mockedServiceDescriptor.id } returns expectedExchangeUrl
    }

    private fun mockUnwrapSignedVcTopLevelFunction(returnedVcContent: VerifiableCredentialContent) {
        mockkStatic("com.microsoft.did.sdk.util.VerifiableCredentialUtilKt")
        every { unwrapSignedVerifiableCredential(any(), serializer) } returns returnedVcContent
    }
}