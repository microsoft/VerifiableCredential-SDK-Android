package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.models.contexts.IdTokenContext
import com.microsoft.did.sdk.credential.service.models.contexts.SelfAttestedClaimContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiableCredentialContext
import com.microsoft.did.sdk.credential.service.protectors.OidcResponseFormatter
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.datasource.db.dao.ReceiptDao
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialDao
import com.microsoft.did.sdk.datasource.db.dao.VerifiableCredentialHolderDao
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import kotlinx.coroutines.runBlocking
import org.junit.Test
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.unwrapSignedVerifiableCredential
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.assert

class VerifiableCredentialHolderRepositoryTest {

    private val database: SdkDatabase = mockk()
    private val mockedIssuanceResponse: IssuanceResponse = mockk()
    private val mockedIssuanceRequest: IssuanceRequest = mockk()
    private val mockedVerifiableCredentialContext: VerifiableCredentialContext = mockk()
    private val mockedIdTokenContext: IdTokenContext = mockk()
    private val mockedSelfAttestedClaimContext: SelfAttestedClaimContext = mockk()
    private val mockedIdentifier: Identifier = mockk()
    private val mockedFormatter: OidcResponseFormatter = mockk()
    private val mockedVerifiableCredentialContent: VerifiableCredentialContent = mockk()

    private val repository: VerifiableCredentialHolderRepository
    private val serializer: Serializer = Serializer()

    private val expectedAudience: String = "audience23430"
    private val expectedVcType: String = "type2309"
    private val expectedSignedResponseToken: String = "responseToke49235"
    private val expectedRpDid: String = "did:ion:test53292"
    private val expectedContractUrl: String = "https://contract.com/2434"
    private val expectedIdTokenContextField: String = "idTokenField48239"
    private val expectedSelfAttestedClaimKey: String = "selfAttestedClaim3454"
    private val expectedIdTokenContextMapping = mapOf(expectedIdTokenContextField to mockedIdTokenContext)
    private val expectedSelfAttestedClaimContext = mapOf(expectedSelfAttestedClaimKey to mockedSelfAttestedClaimContext)
    private val expectedVcToken: String = "vc523094"
    private val expectedVcJti: String = "jti23723"


    init {
        val apiProvider: ApiProvider = mockk()
        setUpDatabase()
        setUpFormatter()
        setUpIssuanceResponse()
        setUpVcContent()
        repository = VerifiableCredentialHolderRepository(database, apiProvider, mockedFormatter, serializer)
        mockkObject(SendVerifiableCredentialIssuanceRequestNetworkOperation::class)
    }

    private fun setUpDatabase() {
        val mockedVchDao: VerifiableCredentialHolderDao = mockk()
        val mockedReceiptDao: ReceiptDao = mockk()
        val mockedVcDao: VerifiableCredentialDao = mockk()
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

    private fun setUpVcContent() {
        mockkObject(JwsToken::class)
        val mockedToken: JwsToken = mockk()
        every { JwsToken.deserialize(expectedVcToken, serializer) } returns mockedToken
        val serializedVcContent = serializer.stringify(VerifiableCredentialContent.serializer(), mockedVerifiableCredentialContent)
        every { mockedToken.content() } returns serializedVcContent
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
        }
    }
}