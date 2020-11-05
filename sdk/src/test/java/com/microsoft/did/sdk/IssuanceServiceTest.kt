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
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.protectors.ExchangeResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.IssuanceResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchContractNetworkOperation
import com.microsoft.did.sdk.datasource.network.credentialOperations.SendVerifiableCredentialIssuanceRequestNetworkOperation
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IssuanceServiceTest {

    private val identifierManager: IdentifierManager = mockk()
    private val masterIdentifier: Identifier = mockk()
    private val pairwiseIdentifier: Identifier = mockk()

    private val mockedResolver: Resolver = mockk()
    private val mockedJwtValidator: JwtValidator = mockk()
    private val exchangeResponseFormatter: ExchangeResponseFormatter = mockk()
    private val issuanceResponseFormatter: IssuanceResponseFormatter = mockk()
    private val exchangeService = ExchangeService(mockk(relaxed = true), exchangeResponseFormatter,
        defaultTestSerializer, mockedJwtValidator)
    private val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
    private val linkedDomainsService =
        spyk(LinkedDomainsService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator))
    private val issuanceService =
        spyk(
            IssuanceService(
                identifierManager,
                exchangeService,
                linkedDomainsService,
                mockk(relaxed = true),
                mockedJwtValidator,
                issuanceResponseFormatter,
                defaultTestSerializer
            )
        )

    private val expectedContractString =
        """{"id":"BusinessCard","display":{"id":"display","locale":"en-US","contract":"BusinessCard","card":{"title":"Business Card","issuedBy":"Adatum Corporation","backgroundColor":"#FFBD02","textColor":"#000000","logo":{"uri":"https://test-relyingparty.azurewebsites.net/images/adatum_corp.png","description":"Adatum Corp Logo"},"description":"This is your business card."},"consent":{"title":"Do you want to get your personal business Card?","instructions":"You will need to present your name and business name in order to get this card."},"claims":{"vc.credentialSubject.firstName":{"type":"String","label":"First Name"},"vc.credentialSubject.lastName":{"type":"String","label":"Last Name"},"vc.credentialSubject.businessName":{"type":"String","label":"Business"}}},"input":{"id":"input","credentialIssuer":"portableIdentities/card/issue","issuer":"did:ion:EiCfeOciEjwupwRQsJC3wMZzz3_M3XIo6bhy7aJkCG6CAQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEMDQwY2lQakUxR0xqLXEyWmRyLVJaXzVlcU8yNFlDMFI5bTlEd2ZHMkdGQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUMyRmQ5UE90emFNcUtMaDNRTFp0Wk43V0RDRHJjdkN4eTNvdlNERDhKRGVRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQ2gtaTFDMW1fM2N4SGJNM3pXemRRdExxMnBvRldaX25FVEJTb0NhT2JZTWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnXzBmOTdlZWZjIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoQ0xsb3JJbGx2M2FWSkRiYkNxM0VHbzU2bWV6Q3RLWkZGcUtvS3RVc3BzIiwieSI6Imh1VG5iTEc3MWU0NDNEeVJkeU5DX3dfc3paR0hVYUcxUHdsMHpXb0h2LUEifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19","attestations":{"selfIssued":{"encrypted":false,"claims":[{"claim":"first_name","type":"String","required":false,"indexed":false},{"claim":"last_name","type":"String","required":false,"indexed":false},{"claim":"business","type":"String","required":false,"indexed":false}],"required":false}}}}"""
    private val expectedContractJwt =
        "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpQ2ZlT2NpRWp3dXB3UlFzSkMzd01aenozX00zWElvNmJoeTdhSmtDRzZDQVE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRU1EUXdZMmxRYWtVeFIweHFMWEV5V21SeUxWSmFYelZsY1U4eU5GbERNRkk1YlRsRWQyWkhNa2RHUVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVNeVJtUTVVRTkwZW1GTmNVdE1hRE5SVEZwMFdrNDNWMFJEUkhKamRrTjRlVE52ZGxORVJEaEtSR1ZSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUTJndGFURkRNVzFmTTJONFNHSk5NM3BYZW1SUmRFeHhNbkJ2UmxkYVgyNUZWRUpUYjBOaFQySlpUV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YekJtT1RkbFpXWmpJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1EweHNiM0pKYkd4Mk0yRldTa1JpWWtOeE0wVkhielUyYldWNlEzUkxXa1pHY1V0dlMzUlZjM0J6SWl3aWVTSTZJbWgxVkc1aVRFYzNNV1UwTkRORWVWSmtlVTVEWDNkZmMzcGFSMGhWWVVjeFVIZHNNSHBYYjBoMkxVRWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnXzBmOTdlZWZjIiwidHlwIjoiSldUIn0.eyJpZCI6IkJ1c2luZXNzQ2FyZCIsImRpc3BsYXkiOnsiaWQiOiJkaXNwbGF5IiwibG9jYWxlIjoiZW4tVVMiLCJjb250cmFjdCI6Imh0dHBzOi8vcG9ydGFibGVpZGVudGl0eWNhcmRzLmF6dXJlLWFwaS5uZXQvZGV2LXYxLjAvNTM2Mjc5ZjYtMTVjYy00NWYyLWJlMmQtNjFlMzUyYjUxZWVmL3BvcnRhYmxlSWRlbnRpdGllcy9jb250cmFjdHMvQnVzaW5lc3NDYXJkIiwiY2FyZCI6eyJ0aXRsZSI6IkJ1c2luZXNzIENhcmQiLCJpc3N1ZWRCeSI6IkFkYXR1bSBDb3Jwb3JhdGlvbiIsImJhY2tncm91bmRDb2xvciI6IiNGRkJEMDIiLCJ0ZXh0Q29sb3IiOiIjMDAwMDAwIiwibG9nbyI6eyJ1cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L2ltYWdlcy9hZGF0dW1fY29ycC5wbmciLCJkZXNjcmlwdGlvbiI6IkFkYXR1bSBDb3JwIExvZ28ifSwiZGVzY3JpcHRpb24iOiJUaGlzIGlzIHlvdXIgYnVzaW5lc3MgY2FyZC4ifSwiY29uc2VudCI6eyJ0aXRsZSI6IkRvIHlvdSB3YW50IHRvIGdldCB5b3VyIHBlcnNvbmFsIGJ1c2luZXNzIENhcmQ_IiwiaW5zdHJ1Y3Rpb25zIjoiWW91IHdpbGwgbmVlZCB0byBwcmVzZW50IHlvdXIgbmFtZSBhbmQgYnVzaW5lc3MgbmFtZSBpbiBvcmRlciB0byBnZXQgdGhpcyBjYXJkLiJ9LCJjbGFpbXMiOnsidmMuY3JlZGVudGlhbFN1YmplY3QuZmlyc3ROYW1lIjp7InR5cGUiOiJTdHJpbmciLCJsYWJlbCI6IkZpcnN0IE5hbWUifSwidmMuY3JlZGVudGlhbFN1YmplY3QubGFzdE5hbWUiOnsidHlwZSI6IlN0cmluZyIsImxhYmVsIjoiTGFzdCBOYW1lIn0sInZjLmNyZWRlbnRpYWxTdWJqZWN0LmJ1c2luZXNzTmFtZSI6eyJ0eXBlIjoiU3RyaW5nIiwibGFiZWwiOiJCdXNpbmVzcyJ9fX0sImlucHV0Ijp7ImlkIjoiaW5wdXQiLCJjcmVkZW50aWFsSXNzdWVyIjoiaHR0cHM6Ly9wb3J0YWJsZWlkZW50aXR5Y2FyZHMuYXp1cmUtYXBpLm5ldC9kZXYtdjEuMC81MzYyNzlmNi0xNWNjLTQ1ZjItYmUyZC02MWUzNTJiNTFlZWYvcG9ydGFibGVJZGVudGl0aWVzL2NhcmQvaXNzdWUiLCJpc3N1ZXIiOiJkaWQ6aW9uOkVpQ2ZlT2NpRWp3dXB3UlFzSkMzd01aenozX00zWElvNmJoeTdhSmtDRzZDQVE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRU1EUXdZMmxRYWtVeFIweHFMWEV5V21SeUxWSmFYelZsY1U4eU5GbERNRkk1YlRsRWQyWkhNa2RHUVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVNeVJtUTVVRTkwZW1GTmNVdE1hRE5SVEZwMFdrNDNWMFJEUkhKamRrTjRlVE52ZGxORVJEaEtSR1ZSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUTJndGFURkRNVzFmTTJONFNHSk5NM3BYZW1SUmRFeHhNbkJ2UmxkYVgyNUZWRUpUYjBOaFQySlpUV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YekJtT1RkbFpXWmpJaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKb1EweHNiM0pKYkd4Mk0yRldTa1JpWWtOeE0wVkhielUyYldWNlEzUkxXa1pHY1V0dlMzUlZjM0J6SWl3aWVTSTZJbWgxVkc1aVRFYzNNV1UwTkRORWVWSmtlVTVEWDNkZmMzcGFSMGhWWVVjeFVIZHNNSHBYYjBoMkxVRWlmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJhdHRlc3RhdGlvbnMiOnsic2VsZklzc3VlZCI6eyJlbmNyeXB0ZWQiOmZhbHNlLCJjbGFpbXMiOlt7ImNsYWltIjoiZmlyc3RfbmFtZSIsInR5cGUiOiJTdHJpbmciLCJyZXF1aXJlZCI6ZmFsc2UsImluZGV4ZWQiOmZhbHNlfSx7ImNsYWltIjoibGFzdF9uYW1lIiwidHlwZSI6IlN0cmluZyIsInJlcXVpcmVkIjpmYWxzZSwiaW5kZXhlZCI6ZmFsc2V9LHsiY2xhaW0iOiJidXNpbmVzcyIsInR5cGUiOiJTdHJpbmciLCJyZXF1aXJlZCI6ZmFsc2UsImluZGV4ZWQiOmZhbHNlfV0sInJlcXVpcmVkIjpmYWxzZX19fSwiaXNzIjoiZGlkOmlvbjpFaUNmZU9jaUVqd3Vwd1JRc0pDM3dNWnp6M19NM1hJbzZiaHk3YUprQ0c2Q0FRPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbEVNRFF3WTJsUWFrVXhSMHhxTFhFeVdtUnlMVkphWHpWbGNVOHlORmxETUZJNWJUbEVkMlpITWtkR1FTSXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVTXlSbVE1VUU5MGVtRk5jVXRNYUROUlRGcDBXazQzVjBSRFJISmpka040ZVROdmRsTkVSRGhLUkdWUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFEyZ3RhVEZETVcxZk0yTjRTR0pOTTNwWGVtUlJkRXh4TW5CdlJsZGFYMjVGVkVKVGIwTmhUMkpaVFdjaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuWHpCbU9UZGxaV1pqSWl3aWRIbHdaU0k2SWtWalpITmhVMlZqY0RJMU5tc3hWbVZ5YVdacFkyRjBhVzl1UzJWNU1qQXhPU0lzSW1wM2F5STZleUpyZEhraU9pSkZReUlzSW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW5naU9pSm9RMHhzYjNKSmJHeDJNMkZXU2tSaVlrTnhNMFZIYnpVMmJXVjZRM1JMV2taR2NVdHZTM1JWYzNCeklpd2llU0k2SW1oMVZHNWlURWMzTVdVME5ETkVlVkprZVU1RFgzZGZjM3BhUjBoVllVY3hVSGRzTUhwWGIwaDJMVUVpZlN3aWNIVnljRzl6WlNJNld5SmhkWFJvSWl3aVoyVnVaWEpoYkNKZGZWMTlmVjE5IiwiaWF0IjoxNjAzMjI5MDEzfQ.jvDa7enSTV2jKJIweVFfJuSbek0Qe5yOc9p1sBCXeQgWOCv5WDr_LeE5akEi-1Wa6QSjcdOI-8Lx6fhAQMw0Hg"
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
    private val mockedIdentifierDocumentService: IdentifierDocumentService = mockk()
    private val mockedIdentifierDocumentServiceEndpoint = "testserviceendpoint.com"
    private val mockedIdentifierDocumentServiceType = "LinkedDomains"

    init {
        coEvery { identifierManager.getMasterIdentifier() } returns Result.Success(masterIdentifier)
        coEvery { identifierManager.createPairwiseIdentifier(masterIdentifier, any()) } returns Result.Success(pairwiseIdentifier)
        mockkConstructor(FetchContractNetworkOperation::class)
        expectedContract = setUpTestContract(expectedContractString)
        mockkConstructor(SendVerifiableCredentialIssuanceRequestNetworkOperation::class)
        coEvery { issuanceResponseFormatter.formatResponse(any(), any(), any(), any()) } returns formattedResponse
    }

    private fun setUpTestContract(expectedContractJwt: String): VerifiableCredentialContract {
        return defaultTestSerializer.decodeFromString(VerifiableCredentialContract.serializer(), expectedContractJwt)
    }

    @Test
    fun `test to get Issuance Request`() {
        val suppliedContractUrl = "BusinessCard"
        val expectedEntityName = "Adatum Corporation"
        val expectedEntityIdentifier =
            "did:ion:EiCfeOciEjwupwRQsJC3wMZzz3_M3XIo6bhy7aJkCG6CAQ?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEMDQwY2lQakUxR0xqLXEyWmRyLVJaXzVlcU8yNFlDMFI5bTlEd2ZHMkdGQSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUMyRmQ5UE90emFNcUtMaDNRTFp0Wk43V0RDRHJjdkN4eTNvdlNERDhKRGVRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQ2gtaTFDMW1fM2N4SGJNM3pXemRRdExxMnBvRldaX25FVEJTb0NhT2JZTWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnXzBmOTdlZWZjIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJoQ0xsb3JJbGx2M2FWSkRiYkNxM0VHbzU2bWV6Q3RLWkZGcUtvS3RVc3BzIiwieSI6Imh1VG5iTEc3MWU0NDNEeVJkeU5DX3dfc3paR0hVYUcxUHdsMHpXb0h2LUEifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"

        coEvery { anyConstructed<FetchContractNetworkOperation>().fire() } returns Result.Success(unwrapContract(expectedContractJwt))
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
        coEvery { linkedDomainsService.fetchAndVerifyLinkedDomains(any()) } returns Result.Success(
            LinkedDomainVerified(mockedIdentifierDocumentServiceEndpoint)
        )
        coEvery { mockedResolver.resolve(expectedContract.input.issuer) } returns Result.Success(mockedIdentifierDocument)
        every { mockedIdentifierDocument.service } returns listOf(mockedIdentifierDocumentService)
        every { mockedIdentifierDocumentService.type } returns mockedIdentifierDocumentServiceType
        every { mockedIdentifierDocumentService.serviceEndpoint } returns listOf(mockedIdentifierDocumentServiceEndpoint)

        runBlocking {
            val actualRequest = issuanceService.getRequest(suppliedContractUrl)
            assertThat(actualRequest).isInstanceOf(Result.Success::class.java)
            assertThat((actualRequest as Result.Success).payload.contractUrl).isEqualTo(suppliedContractUrl)
            assertThat(actualRequest.payload.linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((actualRequest.payload.linkedDomainResult as LinkedDomainVerified).domainUrl).isEqualTo(mockedIdentifierDocumentServiceEndpoint)
            assertThat(actualRequest.payload.entityName).isEqualTo(expectedEntityName)
            assertThat(actualRequest.payload.entityIdentifier).isEqualTo(expectedEntityIdentifier)
        }
    }

    @Test
    fun `test to send Issuance Response`() {
        val suppliedContractUrl = "BusinessCard"
        val issuanceRequest = IssuanceRequest(expectedContract, suppliedContractUrl, LinkedDomainMissing())
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

    private fun unwrapContract(jwsTokenString: String): VerifiableCredentialContract {
        val jwsToken = JwsToken.deserialize(jwsTokenString, defaultTestSerializer)
        return defaultTestSerializer.decodeFromString(VerifiableCredentialContract.serializer(), jwsToken.content())
    }
}

