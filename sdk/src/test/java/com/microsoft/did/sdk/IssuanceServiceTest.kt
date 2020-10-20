// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.models.VerifiableCredentialDescriptor
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.RequestedVcMap
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.protectors.ExchangeResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuanceServiceTest {

    private val serializer = Serializer()
    private val identifierManager: IdentifierManager = mockk()
    private val masterIdentifier: Identifier = mockk()
    private val pairwiseIdentifier: Identifier = mockk()

    private val mockedResolver: Resolver = mockk()
    private val mockedJwtValidator: JwtValidator = mockk()
    private val exchangeResponseFormatter: ExchangeResponseFormatter = mockk()
    private val issuanceResponseFormatter: IssuanceResponseFormatter = mockk()
    private val exchangeService = ExchangeService(mockk(relaxed = true), exchangeResponseFormatter, serializer)
    private val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, serializer)
    private val dnsBindingService = DnsBindingService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator)
    private val issuanceService =
        spyk(IssuanceService(identifierManager, exchangeService, dnsBindingService, mockk(relaxed = true), issuanceResponseFormatter, mockk()))

    private val expectedContractString =
        """{"id":"BusinessCard","display":{"id":"display","locale":"en-US","contract":"https://portableidentitycards.azure-api.net/dev-v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/contracts/BusinessCard","card":{"title":"Business Card","issuedBy":"Adatum Corporation","backgroundColor":"#FFBD02","textColor":"#000000","logo":{"uri":"https://test-relyingparty.azurewebsites.net/images/adatum_corp.png","description":"Adatum Corp Logo"},"description":"This is your business card."},"consent":{"title":"Do you want to get your personal business Card?","instructions":"You will need to present your name and business name in order to get this card."},"claims":{"vc.credentialSubject.firstName":{"type":"String","label":"First Name"},"vc.credentialSubject.lastName":{"type":"String","label":"Last Name"},"vc.credentialSubject.businessName":{"type":"String","label":"Business"}}},"input":{"id":"input","credentialIssuer":"https://portableidentitycards.azure-api.net/dev-v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/card/issue","issuer":"did:ion:EiCfeOciEjwupwRQsJC3wMZzz3_M3XIo6bhy7aJkCG6CAQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEMDQwY2lQakUxR0xqLXEyWmRyLVJaXzVlcU8yNFlDMFI5bTlEd2ZHMkdGQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUMyRmQ5UE90emFNcUtMaDNRTFp0Wk43V0RDRHJjdkN4eTNvdlNERDhKRGVRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQ2gtaTFDMW1fM2N4SGJNM3pXemRRdExxMnBvRldaX25FVEJTb0NhT2JZTWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnXzBmOTdlZWZjIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoQ0xsb3JJbGx2M2FWSkRiYkNxM0VHbzU2bWV6Q3RLWkZGcUtvS3RVc3BzIiwieSI6Imh1VG5iTEc3MWU0NDNEeVJkeU5DX3dfc3paR0hVYUcxUHdsMHpXb0h2LUEifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19","attestations":{"selfIssued":{"encrypted":false,"claims":[{"claim":"first_name","type":"String","required":false,"indexed":false},{"claim":"last_name","type":"String","required":false,"indexed":false},{"claim":"business","type":"String","required":false,"indexed":false}],"required":false}}}}"""
    private val expectedContract: VerifiableCredentialContract
    private val suppliedVcJti = "testJti"
    private val suppliedVcRaw = "testVcRaw"
    private val suppliedVcSubject = "subject"
    private val suppliedVcIssuer = "Issuer"
    private val suppliedIssuedTime = 12345678L
    private val suppliedExpirationTime = 145678998L
    private val suppliedVcContent = VerifiableCredentialContent(
        suppliedVcJti,
        VerifiableCredentialDescriptor(listOf("contexts"), listOf("credentialTypes"), mapOf("credSubKey" to "credSubValue")),
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
    private val formattedResponse = "FORMATTED_RESPONSE"
    private val mockedIdentifierDocument: IdentifierDocument = mockk()
    private val mockedIdentifierDocumentService: JsonObject = mockk()
    private val mockedIdentifierDocumentLinkedDomainEndPoint: JsonElement = mockk()

    init {
        coEvery { identifierManager.getMasterIdentifier() } returns Result.Success(masterIdentifier)
        coEvery { identifierManager.createPairwiseIdentifier(masterIdentifier, any()) } returns Result.Success(pairwiseIdentifier)
        mockkConstructor(FetchContractNetworkOperation::class)
        expectedContract = setUpTestContract(expectedContractString)
        mockkConstructor(SendVerifiableCredentialIssuanceRequestNetworkOperation::class)
        coEvery { issuanceResponseFormatter.formatResponse(any(), any(), any(), any()) } returns formattedResponse
    }

    private fun setUpTestContract(expectedContractJwt: String): VerifiableCredentialContract {
        return serializer.parse(VerifiableCredentialContract.serializer(), expectedContractJwt)
    }

    @Test
    fun `test to get Issuance Request`() {
        val suppliedContractUrl =
            "https://portableidentitycards.azure-api.net/dev-v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/contracts/BusinessCard"
        val expectedDomain = ""
        val expectedEntityName = "Adatum Corporation"
        val expectedEntityIdentifier =
            "did:ion:EiCfeOciEjwupwRQsJC3wMZzz3_M3XIo6bhy7aJkCG6CAQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEMDQwY2lQakUxR0xqLXEyWmRyLVJaXzVlcU8yNFlDMFI5bTlEd2ZHMkdGQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUMyRmQ5UE90emFNcUtMaDNRTFp0Wk43V0RDRHJjdkN4eTNvdlNERDhKRGVRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQ2gtaTFDMW1fM2N4SGJNM3pXemRRdExxMnBvRldaX25FVEJTb0NhT2JZTWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnXzBmOTdlZWZjIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoQ0xsb3JJbGx2M2FWSkRiYkNxM0VHbzU2bWV6Q3RLWkZGcUtvS3RVc3BzIiwieSI6Imh1VG5iTEc3MWU0NDNEeVJkeU5DX3dfc3paR0hVYUcxUHdsMHpXb0h2LUEifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"

        coEvery { anyConstructed<FetchContractNetworkOperation>().fire() } returns Result.Success(expectedContract)
        coEvery { mockedResolver.resolve(expectedContract.input.issuer) } returns Result.Success(mockedIdentifierDocument)
        every { mockedIdentifierDocument.service } returns listOf(mockedIdentifierDocumentService)
        every { mockedIdentifierDocumentService["type"] } returns mockedIdentifierDocumentLinkedDomainEndPoint
        every { mockedIdentifierDocumentLinkedDomainEndPoint.content } returns "LinkedDomain"

        runBlocking {
            val actualRequest = issuanceService.getRequest(suppliedContractUrl)
            assertThat(actualRequest).isInstanceOf(Result.Success::class.java)
            assertThat((actualRequest as Result.Success).payload.contractUrl).isEqualTo(suppliedContractUrl)
            assertThat(actualRequest.payload.domain).isEqualTo(expectedDomain)
            assertThat(actualRequest.payload.entityName).isEqualTo(expectedEntityName)
            assertThat(actualRequest.payload.entityIdentifier).isEqualTo(expectedEntityIdentifier)
        }
    }

    @Test
    fun `test to send Issuance Response`() {
        val suppliedContractUrl =
            "https://portableidentitycards.azure-api.net/dev-v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/contracts/BusinessCard"
        val issuanceRequest = IssuanceRequest(expectedContract, suppliedContractUrl)
        val issuanceResponse = IssuanceResponse(issuanceRequest)
        val requestedVcMap = mapOf(mockk<PresentationAttestation>() to expectedVerifiableCredential) as RequestedVcMap

        coEvery { issuanceService["exchangeVcsInIssuanceRequest"](issuanceResponse, pairwiseIdentifier) } returns Result.Success(
            requestedVcMap
        )
        coEvery {
            issuanceService["formAndSendResponse"](
                issuanceResponse,
                pairwiseIdentifier,
                requestedVcMap,
                Constants.DEFAULT_EXPIRATION_IN_SECONDS
            )
        } returns Result.Success(
            expectedVerifiableCredential
        )

        runBlocking {
            val createdVerifiableCredential = issuanceService.sendResponse(issuanceResponse)
            assertThat(createdVerifiableCredential).isInstanceOf(Result.Success::class.java)
            assertThat((createdVerifiableCredential as Result.Success).payload.jti).isEqualTo(suppliedVcJti)
            assertThat(createdVerifiableCredential.payload.raw).isEqualTo(suppliedVcRaw)
            assertThat(createdVerifiableCredential.payload.contents).isEqualTo(suppliedVcContent)
        }

        coVerify(exactly = 1) {
            issuanceService["exchangeVcsInIssuanceRequest"](issuanceResponse, pairwiseIdentifier)
            issuanceService["formAndSendResponse"](
                issuanceResponse,
                pairwiseIdentifier,
                requestedVcMap,
                Constants.DEFAULT_EXPIRATION_IN_SECONDS
            )
        }
    }
}

