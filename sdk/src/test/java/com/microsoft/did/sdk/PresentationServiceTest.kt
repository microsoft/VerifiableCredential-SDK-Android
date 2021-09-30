// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.net.Uri
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainMissing
import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainVerified
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.protectors.PresentationResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.credentialOperations.FetchPresentationRequestNetworkOperation
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PresentationServiceTest {

    private val identifierManager: IdentifierManager = mockk()
    private val masterIdentifier: Identifier = mockk()

    private val mockedResolver: Resolver = mockk()
    private val mockedJwtValidator: JwtValidator = mockk()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val presentationResponseFormatter: PresentationResponseFormatter = mockk()
    private val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
    private val linkedDomainsService =
        spyk(LinkedDomainsService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator))
    private val presentationService =
        spyk(
            PresentationService(
                identifierManager,
                linkedDomainsService,
                defaultTestSerializer,
                mockedJwtValidator,
                presentationRequestValidator,
                mockk(relaxed = true),
                presentationResponseFormatter
            )
        )
    private val formattedResponse = "FORMATTED_RESPONSE"
    private val expectedPresentationRequestString =
        """{"response_type":"id_token","response_mode":"form_post","client_id":"https://test-relyingparty.azurewebsites.net/verify","redirect_uri":"https://test-relyingparty.azurewebsites.net/verify","iss":"did:ion:EiCXp3lIdFclfl0vvu4qAY8iTumjxiZoKrEmfokRpGcEbg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCWHB4b2VwU2NVdnNmc01UOEpRN0ZqWXpsMHZqZlhjc2RMam9Ga2Y0T3FJUSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUEtU0lSeXNzOTEwMmEzUlpVMVktTFFjVU1JbUdVTE9BVk5rOWFzZ0tkX2hRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQW5QbGJ2djBmQ1RKVnNpNjB4VU9qYUJ1cnBDNWtJVjdsSHhkOFQxZmFtRFEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnbiIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJ1c2UiOiJzaWciLCJhbGciOiJFUzI1NksiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiQ0djSmNmWlVHelc1Qnh5MC1aV1VkQ05WT2tTbDFVRnhIekNycW1LM3RfZyIsInkiOiJNdEZkcnlDVE5BRTRjdmU3Q1RqSkJjQlFlMVQ1YnZVM3MzWmlSdG9zNHVZIn0sInB1cnBvc2UiOlsiYXV0aCIsImdlbmVyYWwiXX1dfX1dfQ","scope":"openid did_authn","state":"OmTlKvp8_qxFbg","nonce":"BxwhKDRMvesRHQ","presentation_definition":{"input_descriptors":[{"id":"BusinessCardCredential","schema":{"uri":["BusinessCardCredential"],"name":"BusinessCardCredential","purpose":"Give us this information please (with cherry on top)!"},"issuance":[{"manifest":"https://portableidentitycards.azure-api.net/dev/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/contracts/BusinessCard"}]}]},"exp":1610660698,"iat":1610660398,"nbf":1610660398,"registration":{"client_name":"Decentralized Identity Team","client_purpose":"Give us this information please (with cherry on top)!","tos_uri":"https://test-relyingparty.azurewebsites.net/tos.html","logo_uri":"https://test-relyingparty.azurewebsites.net/images/did_logo.png"}}"""
    private val expectedPresentationRequestJwt =
        "eyJ0eXAiOiJKV1QiLCJraWQiOiJkaWQ6aW9uOkVpQ1hwM2xJZEZjbGZsMHZ2dTRxQVk4aVR1bWp4aVpvS3JFbWZva1JwR2NFYmc_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1dIQjRiMlZ3VTJOVmRuTm1jMDFVT0VwUk4wWnFXWHBzTUhacVpsaGpjMlJNYW05R2EyWTBUM0ZKVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVFdFUwbFNlWE56T1RFd01tRXpVbHBWTVZrdFRGRmpWVTFKYlVkVlRFOUJWazVyT1dGelowdGtYMmhSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUVc1UWJHSjJkakJtUTFSS1ZuTnBOakI0VlU5cVlVSjFjbkJETld0SlZqZHNTSGhrT0ZReFptRnRSRkVpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5iaUlzSW5SNWNHVWlPaUpGWTJSellWTmxZM0F5TlRack1WWmxjbWxtYVdOaGRHbHZia3RsZVRJd01Ua2lMQ0pxZDJzaU9uc2lhM1I1SWpvaVJVTWlMQ0oxYzJVaU9pSnphV2NpTENKaGJHY2lPaUpGVXpJMU5rc2lMQ0pqY25ZaU9pSnpaV053TWpVMmF6RWlMQ0o0SWpvaVEwZGpTbU5tV2xWSGVsYzFRbmg1TUMxYVYxVmtRMDVXVDJ0VGJERlZSbmhJZWtOeWNXMUxNM1JmWnlJc0lua2lPaUpOZEVaa2NubERWRTVCUlRSamRtVTNRMVJxU2tKalFsRmxNVlExWW5aVk0zTXpXbWxTZEc5ek5IVlpJbjBzSW5CMWNuQnZjMlVpT2xzaVlYVjBhQ0lzSW1kbGJtVnlZV3dpWFgxZGZYMWRmUSNzaWduIiwiYWxnIjoiRVMyNTZLIn0.eyJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJyZXNwb25zZV9tb2RlIjoiZm9ybV9wb3N0IiwiY2xpZW50X2lkIjoiaHR0cHM6Ly90ZXN0LXJlbHlpbmdwYXJ0eS5henVyZXdlYnNpdGVzLm5ldC92ZXJpZnkiLCJyZWRpcmVjdF91cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L3ZlcmlmeSIsInNjb3BlIjoib3BlbmlkIGRpZF9hdXRobiIsInN0YXRlIjoiT21UbEt2cDhfcXhGYmciLCJub25jZSI6IkJ4d2hLRFJNdmVzUkhRIiwiaXNzIjoiZGlkOmlvbjpFaUNYcDNsSWRGY2xmbDB2dnU0cUFZOGlUdW1qeGlab0tyRW1mb2tScEdjRWJnPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbENXSEI0YjJWd1UyTlZkbk5tYzAxVU9FcFJOMFpxV1hwc01IWnFabGhqYzJSTWFtOUdhMlkwVDNGSlVTSXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVRXRVMGxTZVhOek9URXdNbUV6VWxwVk1Wa3RURkZqVlUxSmJVZFZURTlCVms1ck9XRnpaMHRrWDJoUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFFXNVFiR0oyZGpCbVExUktWbk5wTmpCNFZVOXFZVUoxY25CRE5XdEpWamRzU0hoa09GUXhabUZ0UkZFaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuYmlJc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpTENKcWQyc2lPbnNpYTNSNUlqb2lSVU1pTENKMWMyVWlPaUp6YVdjaUxDSmhiR2NpT2lKRlV6STFOa3NpTENKamNuWWlPaUp6WldOd01qVTJhekVpTENKNElqb2lRMGRqU21ObVdsVkhlbGMxUW5oNU1DMWFWMVZrUTA1V1QydFRiREZWUm5oSWVrTnljVzFMTTNSZlp5SXNJbmtpT2lKTmRFWmtjbmxEVkU1QlJUUmpkbVUzUTFScVNrSmpRbEZsTVZRMVluWlZNM016V21sU2RHOXpOSFZaSW4wc0luQjFjbkJ2YzJVaU9sc2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEiLCJyZWdpc3RyYXRpb24iOnsiY2xpZW50X25hbWUiOiJEZWNlbnRyYWxpemVkIElkZW50aXR5IFRlYW0iLCJjbGllbnRfcHVycG9zZSI6IkdpdmUgdXMgdGhpcyBpbmZvcm1hdGlvbiBwbGVhc2UgKHdpdGggY2hlcnJ5IG9uIHRvcCkhIiwidG9zX3VyaSI6Imh0dHBzOi8vdGVzdC1yZWx5aW5ncGFydHkuYXp1cmV3ZWJzaXRlcy5uZXQvdG9zLmh0bWwiLCJsb2dvX3VyaSI6Imh0dHBzOi8vdGVzdC1yZWx5aW5ncGFydHkuYXp1cmV3ZWJzaXRlcy5uZXQvaW1hZ2VzL2RpZF9sb2dvLnBuZyJ9LCJpYXQiOjE2MTA2NjAzOTgsImV4cCI6MTYxMDY2MDY5OCwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiQnVzaW5lc3NDYXJkQ3JlZGVudGlhbCIsInNjaGVtYSI6eyJ1cmkiOlsiQnVzaW5lc3NDYXJkQ3JlZGVudGlhbCJdLCJuYW1lIjoiQnVzaW5lc3NDYXJkQ3JlZGVudGlhbCIsInB1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISJ9LCJpc3N1YW5jZSI6W3sibWFuaWZlc3QiOiJodHRwczovL3BvcnRhYmxlaWRlbnRpdHljYXJkcy5henVyZS1hcGkubmV0L2Rldi81MzYyNzlmNi0xNWNjLTQ1ZjItYmUyZC02MWUzNTJiNTFlZWYvcG9ydGFibGVJZGVudGl0aWVzL2NvbnRyYWN0cy9CdXNpbmVzc0NhcmQifV19XSwibmFtZSI6IkRlY2VudHJhbGl6ZWQgSWRlbnRpdHkgVGVhbSIsInB1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISJ9LCJuYmYiOjE2MTA2NjAzOTgsImp0aSI6ImRhNWY1MDBkLWMyODktNDA5Yy1hYjIyLWVhYzY3NTdlMWZhZiJ9.GqL5DxozP0UcEhcLKHfc8aK4kZK6hU2mYalmj-ffjH8tfStMZwBZxCQ4d_iPupXD6-_HwkAZfw_j5Tih-69QjA"
    private val mockedIdentifierDocument: IdentifierDocument = mockk()
    private val mockedIdentifierDocumentService: IdentifierDocumentService = mockk()
    private val mockedIdentifierDocumentServiceEndpoint = "testserviceendpoint.com"
    private val mockedIdentifierDocumentServiceType = "LinkedDomains"

    private val suppliedOpenIdUrl = "openid://vc/?request_uri=https://test-relyingparty.azurewebsites.net/request/OmTlKvp8_qxFbg"
    private val expectedEntityName = "Decentralized Identity Team"
    private val expectedEntityIdentifier =
        "did:ion:EiCXp3lIdFclfl0vvu4qAY8iTumjxiZoKrEmfokRpGcEbg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlCWHB4b2VwU2NVdnNmc01UOEpRN0ZqWXpsMHZqZlhjc2RMam9Ga2Y0T3FJUSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUEtU0lSeXNzOTEwMmEzUlpVMVktTFFjVU1JbUdVTE9BVk5rOWFzZ0tkX2hRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpQW5QbGJ2djBmQ1RKVnNpNjB4VU9qYUJ1cnBDNWtJVjdsSHhkOFQxZmFtRFEiLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnbiIsInR5cGUiOiJFY2RzYVNlY3AyNTZrMVZlcmlmaWNhdGlvbktleTIwMTkiLCJqd2siOnsia3R5IjoiRUMiLCJ1c2UiOiJzaWciLCJhbGciOiJFUzI1NksiLCJjcnYiOiJzZWNwMjU2azEiLCJ4IjoiQ0djSmNmWlVHelc1Qnh5MC1aV1VkQ05WT2tTbDFVRnhIekNycW1LM3RfZyIsInkiOiJNdEZkcnlDVE5BRTRjdmU3Q1RqSkJjQlFlMVQ1YnZVM3MzWmlSdG9zNHVZIn0sInB1cnBvc2UiOlsiYXV0aCIsImdlbmVyYWwiXX1dfX1dfQ"

    private val invalidSignaturePresentationRequestJwt =
        "eyJ0eXAiOiJKV1QiLCJraWQiOiJkaWQ6aW9uOkVpQ1hwM2xJZEZjbGZsMHZ2dTRxQVk4aVR1bWp4aVpvS3JFbWZva1JwR2NFYmc_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1dIQjRiMlZ3VTJOVmRuTm1jMDFVT0VwUk4wWnFXWHBzTUhacVpsaGpjMlJNYW05R2EyWTBUM0ZKVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVFdFUwbFNlWE56T1RFd01tRXpVbHBWTVZrdFRGRmpWVTFKYlVkVlRFOUJWazVyT1dGelowdGtYMmhSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUVc1UWJHSjJkakJtUTFSS1ZuTnBOakI0VlU5cVlVSjFjbkJETld0SlZqZHNTSGhrT0ZReFptRnRSRkVpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5iaUlzSW5SNWNHVWlPaUpGWTJSellWTmxZM0F5TlRack1WWmxjbWxtYVdOaGRHbHZia3RsZVRJd01Ua2lMQ0pxZDJzaU9uc2lhM1I1SWpvaVJVTWlMQ0oxYzJVaU9pSnphV2NpTENKaGJHY2lPaUpGVXpJMU5rc2lMQ0pqY25ZaU9pSnpaV053TWpVMmF6RWlMQ0o0SWpvaVEwZGpTbU5tV2xWSGVsYzFRbmg1TUMxYVYxVmtRMDVXVDJ0VGJERlZSbmhJZWtOeWNXMUxNM1JmWnlJc0lua2lPaUpOZEVaa2NubERWRTVCUlRSamRtVTNRMVJxU2tKalFsRmxNVlExWW5aVk0zTXpXbWxTZEc5ek5IVlpJbjBzSW5CMWNuQnZjMlVpT2xzaVlYVjBhQ0lzSW1kbGJtVnlZV3dpWFgxZGZYMWRmUSNzaWduIiwiYWxnIjoiRVMyNTZLIn0.eyJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJyZXNwb25zZV9tb2RlIjoiZm9ybV9wb3N0IiwiY2xpZW50X2lkIjoiaHR0cHM6Ly90ZXN0LXJlbHlpbmdwYXJ0eS5henVyZXdlYnNpdGVzLm5ldC92ZXJpZnkiLCJyZWRpcmVjdF91cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L3ZlcmlmeSIsInNjb3BlIjoib3BlbmlkIGRpZF9hdXRobiIsInN0YXRlIjoiT21UbEt2cDhfcXhGYmciLCJub25jZSI6IkJ4d2hLRFJNdmVzUkhRIiwiaXNzIjoiZGlkOmlvbjpFaUNYcDNsSWRGY2xmbDB2dnU0cUFZOGlUdW1qeGlab0tyRW1mb2tScEdjRWJnPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbENXSEI0YjJWd1UyTlZkbk5tYzAxVU9FcFJOMFpxV1hwc01IWnFabGhqYzJSTWFtOUdhMlkwVDNGSlVTSXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVRXRVMGxTZVhOek9URXdNbUV6VWxwVk1Wa3RURkZqVlUxSmJVZFZURTlCVms1ck9XRnpaMHRrWDJoUkluMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFFXNVFiR0oyZGpCbVExUktWbk5wTmpCNFZVOXFZVUoxY25CRE5XdEpWamRzU0hoa09GUXhabUZ0UkZFaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuYmlJc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpTENKcWQyc2lPbnNpYTNSNUlqb2lSVU1pTENKMWMyVWlPaUp6YVdjaUxDSmhiR2NpT2lKRlV6STFOa3NpTENKamNuWWlPaUp6WldOd01qVTJhekVpTENKNElqb2lRMGRqU21ObVdsVkhlbGMxUW5oNU1DMWFWMVZrUTA1V1QydFRiREZWUm5oSWVrTnljVzFMTTNSZlp5SXNJbmtpT2lKTmRFWmtjbmxEVkU1QlJUUmpkbVUzUTFScVNrSmpRbEZsTVZRMVluWlZNM016V21sU2RHOXpOSFZaSW4wc0luQjFjbkJ2YzJVaU9sc2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEiLCJyZWdpc3RyYXRpb24iOnsiY2xpZW50X25hbWUiOiJEZWNlbnRyYWxpemVkIElkZW50aXR5IFRlYW0iLCJjbGllbnRfcHVycG9zZSI6IkdpdmUgdXMgdGhpcyBpbmZvcm1hdGlvbiBwbGVhc2UgKHdpdGggY2hlcnJ5IG9uIHRvcCkhIiwidG9zX3VyaSI6Imh0dHBzOi8vdGVzdC1yZWx5aW5ncGFydHkuYXp1cmV3ZWJzaXRlcy5uZXQvdG9zLmh0bWwiLCJsb2dvX3VyaSI6Imh0dHBzOi8vdGVzdC1yZWx5aW5ncGFydHkuYXp1cmV3ZWJzaXRlcy5uZXQvaW1hZ2VzL2RpZF9sb2dvLnBuZyJ9LCJpYXQiOjE2MTA2NjAzOTgsImV4cCI6MTYxMDY2MDY5OCwicHJlc2VudGF0aW9uX2RlZmluaXRpb24iOnsiaW5wdXRfZGVzY3JpcHRvcnMiOlt7ImlkIjoiQnVzaW5lc3NDYXJkQ3JlZGVudGlhbCIsInNjaGVtYSI6eyJ1cmkiOlsiQnVzaW5lc3NDYXJkQ3JlZGVudGlhbCJdLCJuYW1lIjoiQnVzaW5lc3NDYXJkQ3JlZGVudGlhbCIsInB1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISJ9LCJpc3N1YW5jZSI6W3sibWFuaWZlc3QiOiJodHRwczovL3BvcnRhYmxlaWRlbnRpdHljYXJkcy5henVyZS1hcGkubmV0L2Rldi81MzYyNzlmNi0xNWNjLTQ1ZjItYmUyZC02MWUzNTJiNTFlZWYvcG9ydGFibGVJZGVudGl0aWVzL2NvbnRyYWN0cy9CdXNpbmVzc0NhcmQifV19XSwibmFtZSI6IkRlY2VudHJhbGl6ZWQgSWRlbnRpdHkgVGVhbSIsInB1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISJ9LCJuYmYiOjE2MTA2NjAzOTgsImp0aSI6ImRhNWY1MDBkLWMyODktNDA5Yy1hYjIyLWVhYzY3NTdlMWZhZiJ9.GqL5DxozP0UcEhcLKHfc8aK4kZK6hU2mYalmj-ffjH8tfStMZwBZxCQ4d_iPupXD6-_HwkAh-69QjA"

    init {
        coEvery { identifierManager.getMasterIdentifier() } returns Result.Success(masterIdentifier)
        mockkConstructor(FetchPresentationRequestNetworkOperation::class)
    }

    @Test
    fun `test to get Presentation Request successfully from valid request uri param`() {
        val mockUri = mockk<Uri>()
        mockPresentationRequestFromNetwork()
        mockIdentifierAndLinkedDomains()

        every { presentationService["verifyUri"](suppliedOpenIdUrl) } returns mockUri
        every { mockUri.getQueryParameter("request") } returns null
        every { mockUri.getQueryParameter("request_uri") } returns "https://test-relyingparty.azurewebsites.net/request/OmTlKvp8_qxFbg"
        coJustRun { presentationRequestValidator.validate(any()) }

        runBlocking {
            val actualRequest = presentationService.getRequest(suppliedOpenIdUrl)
            assertThat(actualRequest).isInstanceOf(Result.Success::class.java)
            val actualPresentationRequestContent = (actualRequest as Result.Success).payload.content
            val actualPresentationRequestString = defaultTestSerializer.encodeToString(
                PresentationRequestContent.serializer(),
                actualPresentationRequestContent
            )
            assertThat(actualPresentationRequestString).isEqualTo(expectedPresentationRequestString)
            assertThat(actualRequest.payload.linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((actualRequest.payload.linkedDomainResult as LinkedDomainVerified).domainUrl).isEqualTo(
                mockedIdentifierDocumentServiceEndpoint
            )
            assertThat(actualRequest.payload.entityName).isEqualTo(expectedEntityName)
            assertThat(actualRequest.payload.entityIdentifier).isEqualTo(expectedEntityIdentifier)
        }
    }

    @Test
    fun `test to get Presentation Request successfully from valid request param`() {
        val mockUri = mockk<Uri>()
        mockPresentationRequestFromNetwork()
        mockIdentifierAndLinkedDomains()

        every { presentationService["verifyUri"](suppliedOpenIdUrl) } returns mockUri
        every { mockUri.getQueryParameter("request_uri") } returns null
        every { mockUri.getQueryParameter("request") } returns expectedPresentationRequestJwt
        coJustRun { presentationRequestValidator.validate(any()) }

        runBlocking {
            val actualRequest = presentationService.getRequest(suppliedOpenIdUrl)
            assertThat(actualRequest).isInstanceOf(Result.Success::class.java)
            val actualPresentationRequestContent = (actualRequest as Result.Success).payload.content
            val actualPresentationRequestString = defaultTestSerializer.encodeToString(
                PresentationRequestContent.serializer(),
                actualPresentationRequestContent
            )
            assertThat(actualPresentationRequestString).isEqualTo(expectedPresentationRequestString)
            assertThat(actualRequest.payload.linkedDomainResult).isInstanceOf(LinkedDomainVerified::class.java)
            assertThat((actualRequest.payload.linkedDomainResult as LinkedDomainVerified).domainUrl).isEqualTo(
                mockedIdentifierDocumentServiceEndpoint
            )
            assertThat(actualRequest.payload.entityName).isEqualTo(expectedEntityName)
            assertThat(actualRequest.payload.entityIdentifier).isEqualTo(expectedEntityIdentifier)
        }
    }

    @Test
    fun `test to get Presentation Request failed from request param with invalid signature`() {
        val mockUri = mockk<Uri>()
        mockPresentationRequestWithInvalidSignatureFromNetwork()
        mockIdentifierAndLinkedDomains()

        every { presentationService["verifyUri"](suppliedOpenIdUrl) } returns mockUri
        every { mockUri.getQueryParameter("request_uri") } returns null
        every { mockUri.getQueryParameter("request") } returns invalidSignaturePresentationRequestJwt
        coJustRun { presentationRequestValidator.validate(any()) }

        runBlocking {
            val actualRequest = presentationService.getRequest(suppliedOpenIdUrl)
            assertThat(actualRequest).isInstanceOf(Result.Failure::class.java)
            assertThat((actualRequest as Result.Failure).payload).isInstanceOf(InvalidSignatureException::class.java)
        }
    }

    @Test
    fun `test to get Presentation Request failed from invalid param`() {
        val mockUri = mockk<Uri>()
        mockPresentationRequestFromNetwork()
        mockIdentifierAndLinkedDomains()

        every { presentationService["verifyUri"](suppliedOpenIdUrl) } returns mockUri
        every { mockUri.getQueryParameter("request") } returns null
        every { mockUri.getQueryParameter("request_uri") } returns null
        coJustRun { presentationRequestValidator.validate(any()) }

        runBlocking {
            val actualRequest = presentationService.getRequest(suppliedOpenIdUrl)
            assertThat(actualRequest).isInstanceOf(Result.Failure::class.java)
            assertThat((actualRequest as Result.Failure).payload).isInstanceOf(PresentationException::class.java)
        }
    }

    @Test
    fun `test to send Presentation Response`() {
        val expectedPresentationRequestContent =
            defaultTestSerializer.decodeFromString(PresentationRequestContent.serializer(), expectedPresentationRequestString)
        val presentationRequest = PresentationRequest(expectedPresentationRequestContent, LinkedDomainMissing)
        val presentationResponse = PresentationResponse(presentationRequest)
        every {
            presentationResponseFormatter.formatResponse(
                presentationResponse.requestedVcPresentationSubmissionMap,
                presentationResponse,
                masterIdentifier,
                Constants.DEFAULT_EXPIRATION_IN_SECONDS)
        } returns formattedResponse
        every {
            presentationService["formAndSendResponse"](
                presentationResponse,
                masterIdentifier,
                presentationResponse.requestedVcPresentationSubmissionMap,
                Constants.DEFAULT_EXPIRATION_IN_SECONDS
            )
        } returns Result.Success(Unit)

        runBlocking {
            val presentedResponse = presentationService.sendResponse(presentationResponse)
            assertThat(presentedResponse).isInstanceOf(Result.Success::class.java)
        }

        verify(exactly = 1) {
            presentationService["formAndSendResponse"](
                presentationResponse,
                masterIdentifier,
                presentationResponse.requestedVcPresentationSubmissionMap,
                Constants.DEFAULT_EXPIRATION_IN_SECONDS
            )
        }
    }

    private fun unwrapPresentationContent(jwsTokenString: String): PresentationRequestContent {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        return defaultTestSerializer.decodeFromString(PresentationRequestContent.serializer(), jwsToken.content())
    }

    private fun mockIdentifierAndLinkedDomains() {
        coEvery { linkedDomainsService.fetchAndVerifyLinkedDomains(any()) } returns Result.Success(
            LinkedDomainVerified(mockedIdentifierDocumentServiceEndpoint)
        )
        coEvery { mockedResolver.resolve(expectedEntityIdentifier) } returns Result.Success(mockedIdentifierDocument)
        every { mockedIdentifierDocument.service } returns listOf(mockedIdentifierDocumentService)
        every { mockedIdentifierDocumentService.type } returns mockedIdentifierDocumentServiceType
        every { mockedIdentifierDocumentService.serviceEndpoint } returns listOf(mockedIdentifierDocumentServiceEndpoint)
    }

    private fun mockPresentationRequestFromNetwork() {
        val expectedPresentationRequest = unwrapPresentationContent(expectedPresentationRequestJwt)
        coEvery { anyConstructed<FetchPresentationRequestNetworkOperation>().fire() } returns Result.Success(expectedPresentationRequest)
        coEvery { mockedJwtValidator.verifySignature(any()) } returns true
    }

    private fun mockPresentationRequestWithInvalidSignatureFromNetwork() {
        val expectedPresentationRequest = unwrapPresentationContent(expectedPresentationRequestJwt)
        coEvery { anyConstructed<FetchPresentationRequestNetworkOperation>().fire() } returns Result.Success(expectedPresentationRequest)
        coEvery { mockedJwtValidator.verifySignature(any()) } returns false
    }
}
