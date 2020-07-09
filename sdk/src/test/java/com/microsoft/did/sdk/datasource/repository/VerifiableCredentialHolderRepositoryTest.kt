package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.credential.models.*
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.models.contexts.IdTokenContext
import com.microsoft.did.sdk.credential.service.models.contexts.SelfAttestedClaimContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiableCredentialContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiablePresentationContext
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
    private val mockedVerifiableCredentialContext: VerifiableCredentialContext = mockk()
    private val mockedIdTokenContext: IdTokenContext = mockk()
    private val mockedSelfAttestedClaimContext: SelfAttestedClaimContext = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val mockedFormatter: OidcResponseFormatter = mockk()
    private val mockedVerifiableCredentialContent: VerifiableCredentialContent = mockk()
    private val mockedVpContext: VerifiablePresentationContext = mockk()
    private val mockedVch: VerifiableCredentialHolder = mockk()
    private val mockedVc: VerifiableCredential = mockk()
    private val mockedExchangedVc: VerifiableCredential = mockk()

    private val repository: VerifiableCredentialHolderRepository
    private val serializer: Serializer = Serializer()

    private val expectedAudience: String = "audience23430"
    private val expectedVcType: String = "type2309"
    private val expectedSignedResponseToken: String = "responseToke49235"
    private val expectedRpDid: String = "did:ion:test53292"
    private val expectedNewOwnerDid: String = "did:ion:testNewOwner238"
    private val expectedDid: String = "did:ion:test2398493"
    private val expectedContractUrl: String = "https://contract.com/2434"
    private val expectedIdTokenContextField: String = "idTokenField48239"
    private val expectedSelfAttestedClaimKey: String = "selfAttestedClaim3454"
    private val expectedIdTokenContextMapping = mapOf(expectedIdTokenContextField to mockedIdTokenContext)
    private val expectedSelfAttestedClaimContext = mapOf(expectedSelfAttestedClaimKey to mockedSelfAttestedClaimContext)
    private val expectedVcToken: String = "vc523094"
    private val expectedVcJti: String = "jti23723"
    private val expectedNewVcJti: String = "jti293859"


    init {
        mockkConstructor(SendVerifiableCredentialIssuanceRequestNetworkOperation::class)
        mockkConstructor(SendPresentationResponseNetworkOperation::class)
        val apiProvider: ApiProvider = mockk()
        setUpFormatter()
        setUpIssuanceResponse()
        setUpPresentationResponse()
        setUpExchangeRequest()
        setUpVcContent()
        setUpVpContext()
        setUpDatabase()
        every { mockedIdentifier.id } returns expectedDid
        repository = VerifiableCredentialHolderRepository(database, apiProvider, mockedFormatter, serializer)
    }

    private fun setUpDatabase() {
        val mockedVchDao: VerifiableCredentialHolderDao = mockk()
        val mockedReceiptDao: ReceiptDao = mockk()
        every { database.verifiableCredentialHolderDao() } returns mockedVchDao
        every { database.receiptDao() } returns mockedReceiptDao
        every { database.verifiableCredentialDao() } returns mockedVcDao
    }

    private fun setUpFormatter() {
        every { mockedFormatter.format(
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
        ) } returns expectedSignedResponseToken
    }

    private fun setUpIssuanceResponse() {
        every { mockedIssuanceResponse.audience } returns expectedAudience
        every { mockedIssuanceResponse.request } returns mockedIssuanceRequest
        every { mockedIssuanceRequest.entityIdentifier } returns expectedRpDid
        every { mockedIssuanceRequest.contractUrl } returns expectedContractUrl
        every { mockedIssuanceResponse.getIdTokenContexts() } returns expectedIdTokenContextMapping
        every { mockedIssuanceResponse.getSelfAttestedClaimContexts() } returns expectedSelfAttestedClaimContext
    }

    private fun setUpPresentationResponse() {
        val oidcRequestContent: OidcRequestContent = mockk()
        every { mockedPresentationResponse.audience } returns expectedAudience
        every { mockedPresentationResponse.request } returns mockedPresentationRequest
        every { mockedPresentationRequest.entityIdentifier } returns expectedRpDid
        every { mockedPresentationRequest.content } returns oidcRequestContent
        every { oidcRequestContent.nonce } returns ""
        every { oidcRequestContent.state } returns ""
        every { mockedPresentationResponse.getIdTokenContexts() } returns expectedIdTokenContextMapping
        every { mockedPresentationResponse.getSelfAttestedClaimContexts() } returns expectedSelfAttestedClaimContext
    }

    private fun setUpExchangeRequest() {
        mockkConstructor(ExchangeRequest::class)
        every { anyConstructed<ExchangeRequest>().newOwnerDid } returns expectedNewOwnerDid
        every { anyConstructed<ExchangeRequest>().verifiableCredential } returns mockedExchangedVc
        every { mockedExchangedVc.picId } returns expectedNewVcJti
    }

    private fun setUpVpContext() {
        val mockedVcDescriptor: VerifiableCredentialDescriptor = mockk()
        val mockedServiceDescriptor: ServiceDescriptor = mockk()
        val expectedExchangeUrl = "https://exchange.com/23948"
        every { mockedVpContext.verifiablePresentationHolder } returns mockedVch
        every { mockedVch.owner } returns mockedIdentifier
        every { mockedVch.verifiableCredential } returns mockedVc
        every { mockedVc.contents } returns mockedVerifiableCredentialContent
        every { mockedVc.picId } returns expectedVcJti
        every { mockedVerifiableCredentialContent.vc } returns mockedVcDescriptor
        every { mockedVcDescriptor.exchangeService } returns mockedServiceDescriptor
        every { mockedServiceDescriptor.id } returns expectedExchangeUrl

    }

    private fun setUpVcContent() {
        mockkStatic("com.microsoft.did.sdk.util.VerifiableCredentialUtilKt")
        every { mockedVerifiableCredentialContent.jti } returns expectedVcJti
        every { unwrapSignedVerifiableCredential(any(), serializer) } returns mockedVerifiableCredentialContent
    }

    @Test
    fun `send issuance response successfully`() {
        val expectedVcContexts = mapOf(expectedVcType to mockedVerifiableCredentialContext)
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Success(expectedVcToken)

        runBlocking {
            val actualResult = repository.sendIssuanceResponse(
                mockedIssuanceResponse,
                expectedVcContexts,
                mockedIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload.contents, mockedVerifiableCredentialContent)
            assertEquals((actualResult).payload.jti, expectedVcJti)
            assertEquals((actualResult).payload.picId, expectedVcJti)
            assertEquals((actualResult).payload.raw, expectedVcToken)
        }
    }

    @Test
    fun `send issuance response with failed response from service`() {
        val expectedVcContexts = mapOf(expectedVcType to mockedVerifiableCredentialContext)
        val expectedException = SdkException()
        coEvery { repository.insert(mockedExchangedVc) } returns Unit
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Failure(expectedException)


        runBlocking {
            val actualResult = repository.sendIssuanceResponse(
                mockedIssuanceResponse,
                expectedVcContexts,
                mockedIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertEquals((actualResult as Result.Failure).payload, expectedException)
        }
    }

    @Test
    fun `send presentation response successfully`() {
        val expectedVcContexts = mapOf(expectedVcType to mockedVerifiableCredentialContext)
        coEvery { anyConstructed<SendPresentationResponseNetworkOperation>().fire() } returns Result.Success(Unit)

        runBlocking {
            val actualResult = repository.sendPresentationResponse(
                mockedPresentationResponse,
                expectedVcContexts,
                mockedIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload, Unit)
        }
    }

    @Test
    fun `send presentation response with failed response from service`() {
        val expectedVcContexts = mapOf(expectedVcType to mockedVerifiableCredentialContext)
        val expectedException = SdkException()
        coEvery { anyConstructed<SendPresentationResponseNetworkOperation>().fire() } returns Result.Failure(expectedException)

        runBlocking {
            val actualResult = repository.sendPresentationResponse(
                mockedPresentationResponse,
                expectedVcContexts,
                mockedIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Failure::class.java)
            assertEquals((actualResult as Result.Failure).payload, expectedException)
        }
    }

    @Test
    fun `send exchange response successfully`() {
        every { mockedVch.cardId } returns expectedVcJti
        coEvery { mockedVcDao.insert(mockedVc) } returns Unit
        // val mockedSavedVc: VerifiableCredential = mockk()
        coEvery { repository.getAllVerifiableCredentialsById(expectedVcJti) } returns emptyList()
        coEvery { anyConstructed<SendVerifiableCredentialIssuanceRequestNetworkOperation>().fire() } returns Result.Success(expectedVcToken)

        runBlocking {
            val actualResult = repository.getExchangedVerifiableCredential(
                mockedVpContext,
                mockedIdentifier
            )
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
            assertEquals((actualResult as Result.Success).payload.raw, expectedVcToken)
            assertEquals((actualResult as Result.Success).payload.contents, mockedVerifiableCredentialContent)
            assertEquals((actualResult).payload.jti, expectedVcJti)
            assertEquals((actualResult).payload.picId, expectedVcJti)
            assertEquals((actualResult).payload.raw, expectedVcToken)
        }
    }


}