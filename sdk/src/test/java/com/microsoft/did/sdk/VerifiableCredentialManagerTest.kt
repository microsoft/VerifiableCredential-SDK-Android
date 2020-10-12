/*
// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.IssuanceRequest
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationRequest
import com.microsoft.did.sdk.credential.service.models.attestations.CredentialAttestations
import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.credential.service.validators.PresentationRequestValidator
import com.microsoft.did.sdk.datasource.repository.ReceiptRepository
import com.microsoft.did.sdk.datasource.repository.VerifiableCredentialHolderRepository
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.serializer.Serializer
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VerifiableCredentialManagerTest {
    private val verifiableCredentialHolderRepository: VerifiableCredentialHolderRepository = mockk()
    private val receiptRepository: ReceiptRepository = mockk()
    private val serializer = Serializer()
    private val presentationRequestValidator: PresentationRequestValidator = mockk()
    private val revocationManager = RevocationManager(verifiableCredentialHolderRepository, receiptRepository)
    private val resolver: Resolver = mockk()
    private val cardManager =
        spyk(
            VerifiableCredentialManager(
                verifiableCredentialHolderRepository,
                receiptRepository,
                serializer,
                presentationRequestValidator,
                revocationManager,
                resolver
            )
        )
    private val issuanceRequest: IssuanceRequest
    private val verifiableCredentialHolder: VerifiableCredentialHolder = mockk()
    private val attestations: CredentialAttestations = mockk()
    private val responseAudience = "testEndpointToSendIssuanceRequest"
    private val presentationRequest: PresentationRequest = mockk()
    private val testEntityName = "testEntityName"
    private val testEntityDid = "testEntityDID"
    private val revocationReceipt: RevocationReceipt = mockk()
    private val revokedRPs = arrayOf("did:ion:test")
    private val verifiableCredentialHolderCardId = "testCardId"
    private var issuanceResponse: IssuanceResponse
    private var vcContract: VerifiableCredentialContract = mockk()
    private val issuedBy = "testIssuer"
    private val issuer = "testIssuerDid"
    private val credentialIssuer = "issuanceEndpoint"
    private val mockedPairwiseId: Identifier = mockk()

    init {
        every { vcContract.input.attestations } returns attestations
        every { vcContract.display.card.issuedBy } returns issuedBy
        every { vcContract.input.issuer } returns issuer
        issuanceRequest = IssuanceRequest(vcContract, "testContractUrl")
        every { issuanceRequest.contract.input.credentialIssuer } returns credentialIssuer
        issuanceResponse = IssuanceResponse(issuanceRequest, mockedPairwiseId)
    }

    @Test
    fun `test to create Issuance Response`() {
        every { issuanceRequest.contract.input.credentialIssuer } returns responseAudience
        val issuanceResponse = cardManager.createIssuanceResponse(issuanceRequest, mockedPairwiseId)
        val actualAudience = issuanceResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to create Presentation Response`() {
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest, mockedPairwiseId)
        val actualAudience = presentationResponse.audience
        val expectedAudience = responseAudience
        assertThat(actualAudience).isEqualTo(expectedAudience)
    }

    @Test
    fun `test to save card`() {
        coEvery { verifiableCredentialHolderRepository.insert(verifiableCredentialHolder) } returns Unit
        runBlocking {
            val actualResult = cardManager.saveVch(verifiableCredentialHolder)
            assertThat(actualResult).isInstanceOf(Result.Success::class.java)
        }
    }

    @Test
    fun `test send presentation response`() {
        val responder: Identifier = mockk()
        every { presentationRequest.content.redirectUrl } returns responseAudience
        val presentationResponse = cardManager.createPresentationResponse(presentationRequest, responder)
        every { presentationResponse.request.entityIdentifier } returns testEntityDid
        every { presentationResponse.request.entityName } returns testEntityName
        coEvery { verifiableCredentialHolderRepository.sendPresentationResponse(any(), any(), any()) } returns Result.Success(Unit)
        coJustRun { receiptRepository.createAndSaveReceiptsForVCs(any(), any(), any(), any()) }

        runBlocking {
            val presentationResult = cardManager.sendPresentationResponse(presentationResponse)
            assertThat(presentationResult).isInstanceOf(Result.Success::class.java)
        }

        coVerify(exactly = 1) {
            cardManager.createPresentationResponse(presentationRequest, responder)
            cardManager.sendPresentationResponse(any(), any())
            verifiableCredentialHolderRepository.sendPresentationResponse(any(), any(), any())
            presentationResponse.createReceiptsForPresentedVerifiableCredentials(testEntityDid, testEntityName)
        }
        presentationResponse.requestedVchPresentationSubmissionMap.size.let {
            coVerify(exactly = it) {
                receiptRepository.insert(any())
            }
        }
    }



    @Test
    fun `test deserialize config doc jwt`() {
        val docJwt = "eyJhbGciOiJFUzI1NksiLCJraWQiOiJkaWQ6aW9uOkVpRElqcmFxaTI2T0ptRzN6TTc0TGYwRzZjRWpQWXNDb1VLcDFXT1gzT0FFd2c_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRVVFbGZTRUpNVFVoWGRqWk9WMkk1Wm1nMFUwRXpkVGx3ZFVSYVdsUktUelpQZUVKc1RXa3RjM0JMVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVKUVdUVTBhR3hJWkRodmRHWlBTMXBsTW13MFltaDNiVmc1TkdkdFRuTXdRM1Y1Ym5sNWFWZDVZbkJSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdVM2QycHhRVUpoWW1OcWJGWm1OMjVqUkhsSWJHaFJaVE5oTW1kVFFrWnNZbWxKWTJwb1NVTnRPV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMk5tTTJFek1HSm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKUGFWaGFNelJJU0ZGdlRUSkZhelJQVTBSeGVqVlJWamQzUlhoSlVHdHNPV0phV0hsR1QxSm5kbE5aSWl3aWVTSTZJa2QwU0hOSmFGTm1lamhhY25KRVFXOVFRMm8xVDBoYVgyZG9abVF6V0dwV1RXcG9URzl6Tmxaa2FUZ2lmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkjc2lnX2NmM2EzMGJmIn0.eyJAY29udGV4dCI6Imh0dHBzOi8vaWRlbnRpdHkuZm91bmRhdGlvbi8ud2VsbC1rbm93bi9jb250ZXh0cy9kaWQtY29uZmlndXJhdGlvbi12MC4wLmpzb25sZCIsImxpbmtlZF9kaWRzIjpbeyJzdWIiOiJkaWQ6aW9uOkVpRElqcmFxaTI2T0ptRzN6TTc0TGYwRzZjRWpQWXNDb1VLcDFXT1gzT0FFd2c_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRVVFbGZTRUpNVFVoWGRqWk9WMkk1Wm1nMFUwRXpkVGx3ZFVSYVdsUktUelpQZUVKc1RXa3RjM0JMVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVKUVdUVTBhR3hJWkRodmRHWlBTMXBsTW13MFltaDNiVmc1TkdkdFRuTXdRM1Y1Ym5sNWFWZDVZbkJSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdVM2QycHhRVUpoWW1OcWJGWm1OMjVqUkhsSWJHaFJaVE5oTW1kVFFrWnNZbWxKWTJwb1NVTnRPV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMk5tTTJFek1HSm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKUGFWaGFNelJJU0ZGdlRUSkZhelJQVTBSeGVqVlJWamQzUlhoSlVHdHNPV0phV0hsR1QxSm5kbE5aSWl3aWVTSTZJa2QwU0hOSmFGTm1lamhhY25KRVFXOVFRMm8xVDBoYVgyZG9abVF6V0dwV1RXcG9URzl6Tmxaa2FUZ2lmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJpc3MiOiJkaWQ6aW9uOkVpRElqcmFxaTI2T0ptRzN6TTc0TGYwRzZjRWpQWXNDb1VLcDFXT1gzT0FFd2c_LWlvbi1pbml0aWFsLXN0YXRlPWV5SmtaV3gwWVY5b1lYTm9Jam9pUldsRVVFbGZTRUpNVFVoWGRqWk9WMkk1Wm1nMFUwRXpkVGx3ZFVSYVdsUktUelpQZUVKc1RXa3RjM0JMVVNJc0luSmxZMjkyWlhKNVgyTnZiVzFwZEcxbGJuUWlPaUpGYVVKUVdUVTBhR3hJWkRodmRHWlBTMXBsTW13MFltaDNiVmc1TkdkdFRuTXdRM1Y1Ym5sNWFWZDVZbkJSSW4wLmV5SjFjR1JoZEdWZlkyOXRiV2wwYldWdWRDSTZJa1ZwUkdVM2QycHhRVUpoWW1OcWJGWm1OMjVqUkhsSWJHaFJaVE5oTW1kVFFrWnNZbWxKWTJwb1NVTnRPV2NpTENKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpYMnRsZVhNaU9sdDdJbWxrSWpvaWMybG5YMk5tTTJFek1HSm1JaXdpZEhsd1pTSTZJa1ZqWkhOaFUyVmpjREkxTm1zeFZtVnlhV1pwWTJGMGFXOXVTMlY1TWpBeE9TSXNJbXAzYXlJNmV5SnJkSGtpT2lKRlF5SXNJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbmdpT2lKUGFWaGFNelJJU0ZGdlRUSkZhelJQVTBSeGVqVlJWamQzUlhoSlVHdHNPV0phV0hsR1QxSm5kbE5aSWl3aWVTSTZJa2QwU0hOSmFGTm1lamhhY25KRVFXOVFRMm8xVDBoYVgyZG9abVF6V0dwV1RXcG9URzl6Tmxaa2FUZ2lmU3dpY0hWeWNHOXpaU0k2V3lKaGRYUm9JaXdpWjJWdVpYSmhiQ0pkZlYxOWZWMTkiLCJuYmYiOjE2MDIyNzM2NDUsInZjIjp7IkBjb250ZXh0IjpbImh0dHBzOi8vd3d3LnczLm9yZy8yMDE4L2NyZWRlbnRpYWxzL3YxIiwiaHR0cHM6Ly9pZGVudGl0eS5mb3VuZGF0aW9uLy53ZWxsLWtub3duL2NvbnRleHRzL2RpZC1jb25maWd1cmF0aW9uLXYwLjAuanNvbmxkIl0sImlzc3VlciI6ImRpZDppb246RWlESWpyYXFpMjZPSm1HM3pNNzRMZjBHNmNFalBZc0NvVUtwMVdPWDNPQUV3Zz8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xFVUVsZlNFSk1UVWhYZGpaT1YySTVabWcwVTBFemRUbHdkVVJhV2xSS1R6WlBlRUpzVFdrdGMzQkxVU0lzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUpRV1RVMGFHeElaRGh2ZEdaUFMxcGxNbXcwWW1oM2JWZzVOR2R0VG5Nd1EzVjVibmw1YVZkNVluQlJJbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBSR1UzZDJweFFVSmhZbU5xYkZabU4yNWpSSGxJYkdoUlpUTmhNbWRUUWtac1ltbEpZMnBvU1VOdE9XY2lMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pYzJsblgyTm1NMkV6TUdKbUlpd2lkSGx3WlNJNklrVmpaSE5oVTJWamNESTFObXN4Vm1WeWFXWnBZMkYwYVc5dVMyVjVNakF4T1NJc0ltcDNheUk2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpQYVZoYU16UklTRkZ2VFRKRmF6UlBVMFJ4ZWpWUlZqZDNSWGhKVUd0c09XSmFXSGxHVDFKbmRsTlpJaXdpZVNJNklrZDBTSE5KYUZObWVqaGFjbkpFUVc5UVEybzFUMGhhWDJkb1ptUXpXR3BXVFdwb1RHOXpObFprYVRnaWZTd2ljSFZ5Y0c5elpTSTZXeUpoZFhSb0lpd2laMlZ1WlhKaGJDSmRmVjE5ZlYxOSIsImlzc3VhbmNlRGF0ZSI6IjIwMjAtMTAtMDlUMjA6MDA6NDUuMzExWiIsInR5cGUiOlsiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLCJEb21haW5MaW5rYWdlQ3JlZGVudGlhbCJdLCJjcmVkZW50aWFsU3ViamVjdCI6eyJpZCI6ImRpZDppb246RWlESWpyYXFpMjZPSm1HM3pNNzRMZjBHNmNFalBZc0NvVUtwMVdPWDNPQUV3Zz8taW9uLWluaXRpYWwtc3RhdGU9ZXlKa1pXeDBZVjlvWVhOb0lqb2lSV2xFVUVsZlNFSk1UVWhYZGpaT1YySTVabWcwVTBFemRUbHdkVVJhV2xSS1R6WlBlRUpzVFdrdGMzQkxVU0lzSW5KbFkyOTJaWEo1WDJOdmJXMXBkRzFsYm5RaU9pSkZhVUpRV1RVMGFHeElaRGh2ZEdaUFMxcGxNbXcwWW1oM2JWZzVOR2R0VG5Nd1EzVjVibmw1YVZkNVluQlJJbjAuZXlKMWNHUmhkR1ZmWTI5dGJXbDBiV1Z1ZENJNklrVnBSR1UzZDJweFFVSmhZbU5xYkZabU4yNWpSSGxJYkdoUlpUTmhNbWRUUWtac1ltbEpZMnBvU1VOdE9XY2lMQ0p3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalgydGxlWE1pT2x0N0ltbGtJam9pYzJsblgyTm1NMkV6TUdKbUlpd2lkSGx3WlNJNklrVmpaSE5oVTJWamNESTFObXN4Vm1WeWFXWnBZMkYwYVc5dVMyVjVNakF4T1NJc0ltcDNheUk2ZXlKcmRIa2lPaUpGUXlJc0ltTnlkaUk2SW5ObFkzQXlOVFpyTVNJc0luZ2lPaUpQYVZoYU16UklTRkZ2VFRKRmF6UlBVMFJ4ZWpWUlZqZDNSWGhKVUd0c09XSmFXSGxHVDFKbmRsTlpJaXdpZVNJNklrZDBTSE5KYUZObWVqaGFjbkpFUVc5UVEybzFUMGhhWDJkb1ptUXpXR3BXVFdwb1RHOXpObFprYVRnaWZTd2ljSFZ5Y0c5elpTSTZXeUpoZFhSb0lpd2laMlZ1WlhKaGJDSmRmVjE5ZlYxOSIsIm9yaWdpbiI6Ind3dy5nb29nbGUuY29tIn19fV19.MXxIIJHPTHQzu8UIR-39SezOnSU1hMfzHxWkUAoKDhO5BIG5f2awuX7ooRmuBpevm4VBPqsVVSA9aVwjqQ9nmA"
        val domLinkCreds = cardManager.deserializeConfigDocument(docJwt)
        val expectedDid = "did:ion:EiDIjraqi26OJmG3zM74Lf0G6cEjPYsCoUKp1WOX3OAEwg?-ion-initial-state=eyJkZWx0YV9oYXNoIjoiRWlEUElfSEJMTUhXdjZOV2I5Zmg0U0EzdTlwdURaWlRKTzZPeEJsTWktc3BLUSIsInJlY292ZXJ5X2NvbW1pdG1lbnQiOiJFaUJQWTU0aGxIZDhvdGZPS1plMmw0Ymh3bVg5NGdtTnMwQ3V5bnl5aVd5YnBRIn0.eyJ1cGRhdGVfY29tbWl0bWVudCI6IkVpRGU3d2pxQUJhYmNqbFZmN25jRHlIbGhRZTNhMmdTQkZsYmlJY2poSUNtOWciLCJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljX2tleXMiOlt7ImlkIjoic2lnX2NmM2EzMGJmIiwidHlwZSI6IkVjZHNhU2VjcDI1NmsxVmVyaWZpY2F0aW9uS2V5MjAxOSIsImp3ayI6eyJrdHkiOiJFQyIsImNydiI6InNlY3AyNTZrMSIsIngiOiJPaVhaMzRISFFvTTJFazRPU0RxejVRVjd3RXhJUGtsOWJaWHlGT1JndlNZIiwieSI6Ikd0SHNJaFNmejhacnJEQW9QQ2o1T0haX2doZmQzWGpWTWpoTG9zNlZkaTgifSwicHVycG9zZSI6WyJhdXRoIiwiZ2VuZXJhbCJdfV19fV19"
        assertThat(domLinkCreds.size).isGreaterThan(0)
        assertThat(domLinkCreds.first().iss).isEqualTo(expectedDid)
        assertThat(domLinkCreds.first().sub).isEqualTo(expectedDid)
        assertThat(domLinkCreds.first().vc.issuer).isEqualTo(expectedDid)
        assertThat(domLinkCreds.first().vc.credentialSubject.id).isEqualTo(expectedDid)
    }
}*/
