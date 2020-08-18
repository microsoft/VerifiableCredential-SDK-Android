// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential

import android.content.Context
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import com.microsoft.did.sdk.IdentifierManager
import com.microsoft.did.sdk.VerifiableCredentialManager
import com.microsoft.did.sdk.VerifiableCredentialSdk
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.models.oidc.OidcRequestContentForPresentation
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationSubmissionDescriptor
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptors
import com.microsoft.did.sdk.credential.service.protectors.OidcResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.TokenSigner
import com.microsoft.did.sdk.credential.service.protectors.VerifiablePresentationFormatter
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keys.ellipticCurve.EllipticCurvePairwiseKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class PresentationExchangeTest {

    private val androidSubtle: SubtleCrypto
    private val ellipticCurveSubtleCrypto: EllipticCurveSubtleCrypto
    private val keyStore: AndroidKeyStore
    private val ellipticCurvePairwiseKey: EllipticCurvePairwiseKey
    private val crypto: CryptoOperations
    private val vcManager: VerifiableCredentialManager
    private val identifierManager: IdentifierManager
    private val serializer: Serializer = Serializer()
    private val tokenSigner: TokenSigner
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter
    private val suppliedRequestToken =
        """eyJ0eXAiOiJKV1QiLCJraWQiOiJkaWQ6aW9uOkVpQURxQzdCcUw5endmdnhNQmJQUy1yc1dNa1ZKMG11RnhPbGNxSWwxTFA4eUE_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsQ1NrRnRZalZxVUhRME0xOHdPR3RzU0doc01EWTViVTFTZEZoV1kxQTRRbHBwYjBKb1gwNVNZbEkyZHlJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVFek4yRnZkbEpzYVROMlZsSkJhVnBYWVhWVWFtMWxkVkpqWlRVM1YyWlRRUzFoYlhoc1dHVlJSWEIzSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUVRoeVpIVk1ZM2xHV1hOUU1HVjJaR1ZJTVRWWFEwOURUSEIyUWkxTVVHWldWbmQzV1RacFgwVXlXVkVpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5ibWx1WnlJc0luUjVjR1VpT2lKRlkyUnpZVk5sWTNBeU5UWnJNVlpsY21sbWFXTmhkR2x2Ymt0bGVUSXdNVGtpTENKcWQyc2lPbnNpYTNSNUlqb2lSVU1pTENKamNuWWlPaUp6WldOd01qVTJhekVpTENKNElqb2liVVJqVFY5TVR6QnZWVXQ0UTNaM1lqRTNSbGxUZW5aaVVHSkNTVWhtUjJSSWNUVmtlbk5aYjAxWFl5SXNJbmtpT2lKSWFVRm5kbUZuYmswMFYxZFdRVU16UVhGU2RsaGpVV3RGTFcxV09UVklUV0ZIWVVWbWNVZG5VRmc0SW4wc0luQjFjbkJ2YzJVaU9sc2lZWFYwYUNJc0ltZGxibVZ5WVd3aVhYMWRmWDFkZlEjc2lnbmluZyIsImFsZyI6IkVTMjU2SyJ9.eyJyZXNwb25zZV90eXBlIjoiaWR0b2tlbiIsInJlc3BvbnNlX21vZGUiOiJmb3JtX3Bvc3QiLCJjbGllbnRfaWQiOiJjbGllbnRJZCIsInJlZGlyZWN0X3VyaSI6InJlZGlyZWN0VXJpIiwic2NvcGUiOiJvcGVuaWQgZGlkX2F1dGhuIiwiaXNzIjoiZGlkOmlvbjpFaUFEcUM3QnFMOXp3ZnZ4TUJiUFMtcnNXTWtWSjBtdUZ4T2xjcUlsMUxQOHlBPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbENTa0Z0WWpWcVVIUTBNMTh3T0d0c1NHaHNNRFk1YlUxU2RGaFdZMUE0UWxwcGIwSm9YMDVTWWxJMmR5SXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVRXpOMkZ2ZGxKc2FUTjJWbEpCYVZwWFlYVlVhbTFsZFZKalpUVTNWMlpUUVMxaGJYaHNXR1ZSUlhCM0luMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFFUaHlaSFZNWTNsR1dYTlFNR1YyWkdWSU1UVlhRMDlEVEhCMlFpMU1VR1pXVm5kM1dUWnBYMFV5V1ZFaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuYm1sdVp5SXNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl3TVRraUxDSnFkMnNpT25zaWEzUjVJam9pUlVNaUxDSmpjbllpT2lKelpXTndNalUyYXpFaUxDSjRJam9pYlVSalRWOU1UekJ2VlV0NFEzWjNZakUzUmxsVGVuWmlVR0pDU1VobVIyUkljVFZrZW5OWmIwMVhZeUlzSW5raU9pSklhVUZuZG1GbmJrMDBWMWRXUVVNelFYRlNkbGhqVVd0RkxXMVdPVFZJVFdGSFlVVm1jVWRuVUZnNEluMHNJbkIxY25CdmMyVWlPbHNpWVhWMGFDSXNJbWRsYm1WeVlXd2lYWDFkZlgxZGZRIiwicmVnaXN0cmF0aW9uIjp7ImNsaWVudF9uYW1lIjoiY2xpZW50TmFtZSIsImNsaWVudF9wdXJwb3NlIjoiY2xpZW50UHVycG9zZSIsInRvc191cmkiOiJ0b3NVcmkiLCJsb2dvX3VyaSI6ImxvZ29VcmkifSwiaWF0IjoxNTk2NjEyMjk3LCJleHAiOjE1OTcyMTcwOTcsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6ImlucHV0RGVzY3JpcHRvcklkIiwic2NoZW1hIjp7InVybCI6WyJodHRwczovL3NjaGVtYS5leGFtcGxlLmNvbS9kcml2aW5nbGljZW5zZSJdLCJuYW1lIjoic2NoZW1hTmFtZSIsInB1cnBvc2UiOiJzY2hlbWFQdXJwb3NlIn0sImlzc3VhbmNlIjpbeyJkaWQiOiJkaWQ6dXNlciIsIm1hbmlmZXN0IjoiaHR0cHM6Ly9jb250cmFjdC5leGFtcGxlLmNvbSJ9XX1dLCJuYW1lIjoicHJlc2VudGF0aW9uRGVmaW5pdGlvbk5hbWUiLCJwdXJwb3NlIjoicHJlc2VudGF0aW9uRGVmaW5pdGlvblB1cnBvc2UifX0.yzo2VdVGI6tCKKsHjM9jJJ_nbtX8fWgpx0b2jkZtNyOJ1gsallk-C8kCnkMzwlfr0QPle63nUaTTK5m9lG7hNQ"""
    private val suppliedPERequest =
        """eyJyZXNwb25zZV90eXBlIjoiaWR0b2tlbiIsInJlc3BvbnNlX21vZGUiOiJmb3JtX3Bvc3QiLCJjbGllbnRfaWQiOiJjbGllbnRJZCIsInJlZGlyZWN0X3VyaSI6InJlZGlyZWN0VXJpIiwic2NvcGUiOiJvcGVuaWQgZGlkX2F1dGhuIiwiaXNzIjoiZGlkOmlvbjpFaUFEcUM3QnFMOXp3ZnZ4TUJiUFMtcnNXTWtWSjBtdUZ4T2xjcUlsMUxQOHlBPy1pb24taW5pdGlhbC1zdGF0ZT1leUprWld4MFlWOW9ZWE5vSWpvaVJXbENTa0Z0WWpWcVVIUTBNMTh3T0d0c1NHaHNNRFk1YlUxU2RGaFdZMUE0UWxwcGIwSm9YMDVTWWxJMmR5SXNJbkpsWTI5MlpYSjVYMk52YlcxcGRHMWxiblFpT2lKRmFVRXpOMkZ2ZGxKc2FUTjJWbEpCYVZwWFlYVlVhbTFsZFZKalpUVTNWMlpUUVMxaGJYaHNXR1ZSUlhCM0luMC5leUoxY0dSaGRHVmZZMjl0YldsMGJXVnVkQ0k2SWtWcFFUaHlaSFZNWTNsR1dYTlFNR1YyWkdWSU1UVlhRMDlEVEhCMlFpMU1VR1pXVm5kM1dUWnBYMFV5V1ZFaUxDSndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqWDJ0bGVYTWlPbHQ3SW1sa0lqb2ljMmxuYm1sdVp5SXNJblI1Y0dVaU9pSkZZMlJ6WVZObFkzQXlOVFpyTVZabGNtbG1hV05oZEdsdmJrdGxlVEl3TVRraUxDSnFkMnNpT25zaWEzUjVJam9pUlVNaUxDSmpjbllpT2lKelpXTndNalUyYXpFaUxDSjRJam9pYlVSalRWOU1UekJ2VlV0NFEzWjNZakUzUmxsVGVuWmlVR0pDU1VobVIyUkljVFZrZW5OWmIwMVhZeUlzSW5raU9pSklhVUZuZG1GbmJrMDBWMWRXUVVNelFYRlNkbGhqVVd0RkxXMVdPVFZJVFdGSFlVVm1jVWRuVUZnNEluMHNJbkIxY25CdmMyVWlPbHNpWVhWMGFDSXNJbWRsYm1WeVlXd2lYWDFkZlgxZGZRIiwicmVnaXN0cmF0aW9uIjp7ImNsaWVudF9uYW1lIjoiY2xpZW50TmFtZSIsImNsaWVudF9wdXJwb3NlIjoiY2xpZW50UHVycG9zZSIsInRvc191cmkiOiJ0b3NVcmkiLCJsb2dvX3VyaSI6ImxvZ29VcmkifSwiaWF0IjoxNTk2NjEyMjk3LCJleHAiOjE1OTcyMTcwOTcsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6ImlucHV0RGVzY3JpcHRvcklkIiwic2NoZW1hIjp7InVybCI6WyJodHRwczovL3NjaGVtYS5leGFtcGxlLmNvbS9kcml2aW5nbGljZW5zZSJdLCJuYW1lIjoic2NoZW1hTmFtZSIsInB1cnBvc2UiOiJzY2hlbWFQdXJwb3NlIn0sImlzc3VhbmNlIjpbeyJkaWQiOiJkaWQ6dXNlciIsIm1hbmlmZXN0IjoiaHR0cHM6Ly9jb250cmFjdC5leGFtcGxlLmNvbSJ9XX1dLCJuYW1lIjoicHJlc2VudGF0aW9uRGVmaW5pdGlvbk5hbWUiLCJwdXJwb3NlIjoicHJlc2VudGF0aW9uRGVmaW5pdGlvblB1cnBvc2UifX0"""
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    init {
        VerifiableCredentialSdk.init(context)
        vcManager = VerifiableCredentialSdk.verifiableCredentialManager
        identifierManager = VerifiableCredentialSdk.identifierManager
        keyStore = AndroidKeyStore(context, serializer)
        androidSubtle = AndroidSubtle(keyStore)
        ellipticCurvePairwiseKey = EllipticCurvePairwiseKey()
        ellipticCurveSubtleCrypto = EllipticCurveSubtleCrypto(androidSubtle, serializer)
        crypto = CryptoOperations(ellipticCurveSubtleCrypto, keyStore, ellipticCurvePairwiseKey)
        tokenSigner = TokenSigner(crypto, serializer)
        verifiablePresentationFormatter = VerifiablePresentationFormatter(serializer, tokenSigner)
    }

    @Test
    fun testPresentationExchange() {
        val oidcRequestContent =
            serializer.parse(OidcRequestContentForPresentation.serializer(), JwsToken.deserialize(suppliedRequestToken, serializer).content())
        var issuanceResponse: IssuanceResponse
        var issuancePairwiseIdentifier: Identifier
        var presentationPairwiseIdentifier: Identifier

        val presentationRequest =
            PresentationRequest(suppliedRequestToken, oidcRequestContent, oidcRequestContent.credentialPresentationDefinition)
        val credentialPresentationInputDescriptorsMock: CredentialPresentationInputDescriptors = mockk()
        presentationRequest.credentialPresentationDefinition?.credentialPresentationInputDescriptors?.first()?.credentialIssuanceMetadataList?.first()?.issuerContract =
            "https://portableidentitycards.azure-api.net/dev-v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/contracts/IdentityCard"
        presentationRequest.credentialPresentationDefinition?.credentialPresentationInputDescriptors?.first()?.id = "identity_card"

        var oidcPresentationResponse: String

        runBlocking {
            val issuanceRequest =
                getIssuanceRequestFromContractUrl("https://portableidentitycards.azure-api.net/dev-v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/portableIdentities/contracts/IdentityCard")
            issuanceResponse = vcManager.createIssuanceResponse(issuanceRequest)
            println("issuance response is $issuanceResponse")

            val identifier = getMasterIdentifier()
            issuancePairwiseIdentifier = getPairwiseIdentifier(identifier, issuanceRequest.entityIdentifier)

            val vchResult = vcManager.sendIssuanceResponse(issuanceResponse, issuancePairwiseIdentifier)
            var vch: VerifiableCredentialHolder = mockk()
            var pairwiseVC: VerifiableCredentialHolder = mockk()
            if (vchResult is Result.Success) {
                vch = vchResult.payload
                println("vc raw is ${vch.verifiableCredential.raw}")
            } else if(vchResult is Result.Failure)
                println("Issuance Failed with  ${vchResult.payload}")

            val credentialPresentationInputDescriptors = presentationRequest.credentialPresentationDefinition?.credentialPresentationInputDescriptors?.first()
                ?: credentialPresentationInputDescriptorsMock
            val requestedPairwiseVC = mutableMapOf(credentialPresentationInputDescriptors to vch)
            presentationPairwiseIdentifier = getPairwiseIdentifier(identifier, presentationRequest.entityIdentifier)
            val pairwiseResult = vcManager.getExchangedVcs(true, requestedPairwiseVC, presentationPairwiseIdentifier)
            if(pairwiseResult is Result.Success) {
                pairwiseVC = pairwiseResult.payload.values.first()
                println("pairwise vc raw is ${pairwiseVC.verifiableCredential.raw}")
            }
            val presentationResponse = vcManager.createPresentationResponse(presentationRequest)
            presentationResponse.addRequestedVchClaims(credentialPresentationInputDescriptors, pairwiseVC)
            println("PE response is $presentationResponse")
/*            val formattedResponseResult = vcManager.sendPresentationResponse(presentationResponse, pairwiseIdentifier)
            if(formattedResponseResult is Result.Success)
                println("formatted response - oidc is ${formattedResponseResult.payload}")*/

            oidcPresentationResponse = createOidcResponseFromPresentationResponse(presentationResponse, presentationPairwiseIdentifier)
            println("OIDC Response is $oidcPresentationResponse")
        }
    }

    @Test
    fun validatePERequest() {
        val serializer = Serializer()
        val oidcRequestContent =
            serializer.parse(OidcRequestContentForPresentation.serializer(), JwsToken.deserialize(suppliedRequestToken, serializer).content())
        val request = PresentationRequest(suppliedRequestToken, oidcRequestContent, oidcRequestContent.credentialPresentationDefinition)
        runBlocking {
            val validationResult = vcManager.isRequestValid(request)
            assertThat(validationResult).isInstanceOf(Result.Success::class.java)
        }
    }

    private fun getIssuanceRequestFromContractUrl(contractUrl: String): IssuanceRequest {
        var issuanceRequest: IssuanceRequest = mockk()
        runBlocking {
            val result = vcManager.getIssuanceRequest(contractUrl)
            val mockedIssReq = mockIssuance()
            issuanceRequest = if (result is Result.Success)
                result.payload
            else
                mockedIssReq
        }
        return issuanceRequest
    }

    private fun mockIssuance(): IssuanceRequest {
        var vcContract: VerifiableCredentialContract = mockk()
        val attestations: CredentialAttestations = mockk()
        val issuedBy = "testIssuer"
        val issuer = "testIssuerDid"
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        val mockedIssReq = IssuanceRequest(vcContract, "testContractUrl", vcContract.input.attestations)
        val credentialIssuer = "issuanceEndpoint"
        every { mockedIssReq.contract.input.credentialIssuer } returns credentialIssuer
        return mockedIssReq
    }

    private fun getMasterIdentifier(): Identifier {
        val mockedIdentifier: Identifier = mockk()
        var identifier: Identifier = mockk()
        runBlocking {
            val identifierResult = identifierManager.getMasterIdentifier()
            identifier = if (identifierResult is Result.Success) {
                identifierResult.payload
            } else {
                mockedIdentifier
            }
        }
        return identifier
    }

    private fun getPairwiseIdentifier(identifier: Identifier, rpIdentifier: String): Identifier {
        var pairwiseIdentifier: Identifier = mockk()
        runBlocking {
            val pairwiseIdentifierResult = identifierManager.createPairwiseIdentifier(identifier, rpIdentifier)
            pairwiseIdentifier = if (pairwiseIdentifierResult is Result.Success) {
                pairwiseIdentifierResult.payload
            } else {
                identifier
            }
        }
        return pairwiseIdentifier
    }

    private fun createOidcResponseFromPresentationResponse(response: PresentationResponse, responder: Identifier): String {
        val formatter = OidcResponseFormatter(crypto, serializer, verifiablePresentationFormatter, tokenSigner)
        val presentationSubmission = mutableListOf<CredentialPresentationSubmissionDescriptor>()
        response.getRequestedVchClaims().forEach { presentationSubmission.add(transformReqToResp(it.component1())) }
        return formatter.formatPresentationResponse(
            responder = responder,
//            credentialPresentationSubmission = CredentialPresentationSubmission(presentationSubmission),
            requestedVchPresentationSubmissionMap = response.getRequestedVchClaims(),
            expiryInSeconds = 604800,
            presentationResponse = response
        )
    }

    private fun transformReqToResp(credentialPresentationInputDescriptor: CredentialPresentationInputDescriptors): CredentialPresentationSubmissionDescriptor {
        val credentialPresentationSubmissionDescriptor = CredentialPresentationSubmissionDescriptor(
            credentialPresentationInputDescriptor.id,
            "$.attestations.presentations.${credentialPresentationInputDescriptor.id}"
        )
        credentialPresentationSubmissionDescriptor.credentialFormat = "jwt"
        credentialPresentationSubmissionDescriptor.credentialEncoding = "base64Url"
        return credentialPresentationSubmissionDescriptor
    }
}