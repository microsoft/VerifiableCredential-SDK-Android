// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.credential.service.protectors.ExchangeResponseFormatter
import com.microsoft.did.sdk.credential.service.protectors.PresentationResponseFormatter
import com.microsoft.did.sdk.credential.service.validators.JwtDomainLinkageCredentialValidator
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.credential.service.validators.OidcPresentationRequestValidator
import com.microsoft.did.sdk.di.defaultTestSerializer
import com.microsoft.did.sdk.identifier.resolvers.Resolver
import com.microsoft.did.sdk.util.controlflow.Result
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Test

class PresentationServiceTest {

    private val identifierManager: IdentifierManager = mockk()
    private val mockedResolver: Resolver = mockk()
    private val mockedJwtValidator: JwtValidator = mockk()
    private val exchangeResponseFormatter: ExchangeResponseFormatter = mockk()
    private val presentationResponseFormatter: PresentationResponseFormatter = mockk()
    private val exchangeService = ExchangeService(
        mockk(relaxed = true), exchangeResponseFormatter,
        defaultTestSerializer, mockedJwtValidator)
    private val mockedOidcPresentationRequestValidator = OidcPresentationRequestValidator()
    private val mockedJwtDomainLinkageCredentialValidator = JwtDomainLinkageCredentialValidator(mockedJwtValidator, defaultTestSerializer)
    private val linkedDomainsService =
        spyk(LinkedDomainsService(mockk(relaxed = true), mockedResolver, mockedJwtDomainLinkageCredentialValidator))
    private val presentationService =
        spyk(
            PresentationService(
                identifierManager,
                exchangeService,
                linkedDomainsService,
                defaultTestSerializer,
                mockedJwtValidator,
                mockedOidcPresentationRequestValidator,
                mockk(relaxed = true),
                presentationResponseFormatter
            )
        )

    private val expectedPresentationRequestString =
        """{
  "response_type": "id_token",
  "response_mode": "form_post",
  "client_id": "https://test-relyingparty.azurewebsites.net/verify",
  "redirect_uri": "https://test-relyingparty.azurewebsites.net/verify",
  "scope": "openid did_authn",
  "state": "0Qabvmm1KLD23w",
  "nonce": "X6M24JhpeJKgyw",
  "iss": "did:ion:EiDN4VhftlVKqGIzf6xboqG9lnRd323cq_KSwVzyP2VdNg:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWduIiwicHVibGljS2V5SndrIjp7ImNydiI6InNlY3AyNTZrMSIsImt0eSI6IkVDIiwieCI6Im8zLWt0bjByUHlFSVFuTWg0LVFYVzB4LXpHaWhPeWZuTlNaNG4xT3JjYnciLCJ5IjoicW5MOFZ5ejNwYmVxZWpuTk03QnE2S1hPQTk2MjNlMFRtc1N3cjAxeGUxMCJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImtleUFncmVlbWVudCJdLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5In1dLCJzZXJ2aWNlcyI6W119fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUNUcHFub1dLRXpYQ0dRZ0VMaW95Z0JVaTNGSkxQbjNhbUNlcmVmWXN6YVpnIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlCazFSdHJBTGlJYUFSR05TR1NwYS13RV9tZDRGcWt0SVhCMFROMzFxUHQ1dyIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQTEyc2V5cG8ya01IdDQwMnllT2h0N2k5dF9mUjVfOUg4R25uYXctWmlXbHcifX0",
  "registration": {
    "client_name": "Decentralized Identity Team",
    "client_purpose": "Give us this information please (with cherry on top)!",
    "tos_uri": "https://test-relyingparty.azurewebsites.net/tos.html",
    "logo_uri": "https://test-relyingparty.azurewebsites.net/images/did_logo.png"
  },
  "iat": 1616706894,
  "exp": 1616707194,
  "presentation_definition": {
    "input_descriptors": [
      {
        "id": "BusinessCardCredential",
        "schema": {
          "uri": [
            "BusinessCardCredential"
          ],
          "name": "BusinessCardCredential",
          "purpose": "Give us this information please (with cherry on top)!"
        },
        "issuance": [
          {
            "manifest": "https://dev.did.msidentity.com/v1.0/536279f6-15cc-45f2-be2d-61e352b51eef/verifiableCredential/contracts/BusinessCard"
          }
        ]
      }
    ],
    "name": "Decentralized Identity Team",
    "purpose": "Give us this information please (with cherry on top)!"
  },
  "nbf": 1616706894,
  "jti": "09df6908-bc30-456b-92e4-1a52670c69e7"
}"""
    private val expectedPresentationRequestJwt =
        "eyJ0eXAiOiJKV1QiLCJraWQiOiJkaWQ6aW9uOkVpRE40VmhmdGxWS3FHSXpmNnhib3FHOWxuUmQzMjNjcV9LU3dWenlQMlZkTmc6ZXlKa1pXeDBZU0k2ZXlKd1lYUmphR1Z6SWpwYmV5SmhZM1JwYjI0aU9pSnlaWEJzWVdObElpd2laRzlqZFcxbGJuUWlPbnNpY0hWaWJHbGpTMlY1Y3lJNlczc2lhV1FpT2lKemFXZHVJaXdpY0hWaWJHbGpTMlY1U25kcklqcDdJbU55ZGlJNkluTmxZM0F5TlRack1TSXNJbXQwZVNJNklrVkRJaXdpZUNJNkltOHpMV3QwYmpCeVVIbEZTVkZ1VFdnMExWRllWekI0TFhwSGFXaFBlV1p1VGxOYU5HNHhUM0pqWW5jaUxDSjVJam9pY1c1TU9GWjVlak53WW1WeFpXcHVUazAzUW5FMlMxaFBRVGsyTWpObE1GUnRjMU4zY2pBeGVHVXhNQ0o5TENKd2RYSndiM05sY3lJNld5SmhkWFJvWlc1MGFXTmhkR2x2YmlJc0ltdGxlVUZuY21WbGJXVnVkQ0pkTENKMGVYQmxJam9pUldOa2MyRlRaV053TWpVMmF6RldaWEpwWm1sallYUnBiMjVMWlhreU1ERTVJbjFkTENKelpYSjJhV05sY3lJNlcxMTlmVjBzSW5Wd1pHRjBaVU52YlcxcGRHMWxiblFpT2lKRmFVTlVjSEZ1YjFkTFJYcFlRMGRSWjBWTWFXOTVaMEpWYVROR1NreFFiak5oYlVObGNtVm1XWE42WVZwbkluMHNJbk4xWm1acGVFUmhkR0VpT25zaVpHVnNkR0ZJWVhOb0lqb2lSV2xDYXpGU2RISkJUR2xKWVVGU1IwNVRSMU53WVMxM1JWOXRaRFJHY1d0MFNWaENNRlJPTXpGeFVIUTFkeUlzSW5KbFkyOTJaWEo1UTI5dGJXbDBiV1Z1ZENJNklrVnBRVEV5YzJWNWNHOHlhMDFJZERRd01ubGxUMmgwTjJrNWRGOW1ValZmT1VnNFIyNXVZWGN0V21sWGJIY2lmWDAjc2lnbiIsImFsZyI6IkVTMjU2SyJ9.eyJyZXNwb25zZV90eXBlIjoiaWRfdG9rZW4iLCJyZXNwb25zZV9tb2RlIjoiZm9ybV9wb3N0IiwiY2xpZW50X2lkIjoiaHR0cHM6Ly90ZXN0LXJlbHlpbmdwYXJ0eS5henVyZXdlYnNpdGVzLm5ldC92ZXJpZnkiLCJyZWRpcmVjdF91cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L3ZlcmlmeSIsInNjb3BlIjoib3BlbmlkIGRpZF9hdXRobiIsInN0YXRlIjoiMFFhYnZtbTFLTEQyM3ciLCJub25jZSI6Ilg2TTI0SmhwZUpLZ3l3IiwiaXNzIjoiZGlkOmlvbjpFaURONFZoZnRsVktxR0l6ZjZ4Ym9xRzlsblJkMzIzY3FfS1N3Vnp5UDJWZE5nOmV5SmtaV3gwWVNJNmV5SndZWFJqYUdWeklqcGJleUpoWTNScGIyNGlPaUp5WlhCc1lXTmxJaXdpWkc5amRXMWxiblFpT25zaWNIVmliR2xqUzJWNWN5STZXM3NpYVdRaU9pSnphV2R1SWl3aWNIVmliR2xqUzJWNVNuZHJJanA3SW1OeWRpSTZJbk5sWTNBeU5UWnJNU0lzSW10MGVTSTZJa1ZESWl3aWVDSTZJbTh6TFd0MGJqQnlVSGxGU1ZGdVRXZzBMVkZZVnpCNExYcEhhV2hQZVdadVRsTmFORzR4VDNKalluY2lMQ0o1SWpvaWNXNU1PRlo1ZWpOd1ltVnhaV3B1VGswM1FuRTJTMWhQUVRrMk1qTmxNRlJ0YzFOM2NqQXhlR1V4TUNKOUxDSndkWEp3YjNObGN5STZXeUpoZFhSb1pXNTBhV05oZEdsdmJpSXNJbXRsZVVGbmNtVmxiV1Z1ZENKZExDSjBlWEJsSWpvaVJXTmtjMkZUWldOd01qVTJhekZXWlhKcFptbGpZWFJwYjI1TFpYa3lNREU1SW4xZExDSnpaWEoyYVdObGN5STZXMTE5ZlYwc0luVndaR0YwWlVOdmJXMXBkRzFsYm5RaU9pSkZhVU5VY0hGdWIxZExSWHBZUTBkUlowVk1hVzk1WjBKVmFUTkdTa3hRYmpOaGJVTmxjbVZtV1hONllWcG5JbjBzSW5OMVptWnBlRVJoZEdFaU9uc2laR1ZzZEdGSVlYTm9Jam9pUldsQ2F6RlNkSEpCVEdsSllVRlNSMDVUUjFOd1lTMTNSVjl0WkRSR2NXdDBTVmhDTUZST016RnhVSFExZHlJc0luSmxZMjkyWlhKNVEyOXRiV2wwYldWdWRDSTZJa1ZwUVRFeWMyVjVjRzh5YTAxSWREUXdNbmxsVDJoME4yazVkRjltVWpWZk9VZzRSMjV1WVhjdFdtbFhiSGNpZlgwIiwicmVnaXN0cmF0aW9uIjp7ImNsaWVudF9uYW1lIjoiRGVjZW50cmFsaXplZCBJZGVudGl0eSBUZWFtIiwiY2xpZW50X3B1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISIsInRvc191cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L3Rvcy5odG1sIiwibG9nb191cmkiOiJodHRwczovL3Rlc3QtcmVseWluZ3BhcnR5LmF6dXJld2Vic2l0ZXMubmV0L2ltYWdlcy9kaWRfbG9nby5wbmcifSwiaWF0IjoxNjE2NzA2ODk0LCJleHAiOjE2MTY3MDcxOTQsInByZXNlbnRhdGlvbl9kZWZpbml0aW9uIjp7ImlucHV0X2Rlc2NyaXB0b3JzIjpbeyJpZCI6IkJ1c2luZXNzQ2FyZENyZWRlbnRpYWwiLCJzY2hlbWEiOnsidXJpIjpbIkJ1c2luZXNzQ2FyZENyZWRlbnRpYWwiXSwibmFtZSI6IkJ1c2luZXNzQ2FyZENyZWRlbnRpYWwiLCJwdXJwb3NlIjoiR2l2ZSB1cyB0aGlzIGluZm9ybWF0aW9uIHBsZWFzZSAod2l0aCBjaGVycnkgb24gdG9wKSEifSwiaXNzdWFuY2UiOlt7Im1hbmlmZXN0IjoiaHR0cHM6Ly9kZXYuZGlkLm1zaWRlbnRpdHkuY29tL3YxLjAvNTM2Mjc5ZjYtMTVjYy00NWYyLWJlMmQtNjFlMzUyYjUxZWVmL3ZlcmlmaWFibGVDcmVkZW50aWFsL2NvbnRyYWN0cy9CdXNpbmVzc0NhcmQifV19XSwibmFtZSI6IkRlY2VudHJhbGl6ZWQgSWRlbnRpdHkgVGVhbSIsInB1cnBvc2UiOiJHaXZlIHVzIHRoaXMgaW5mb3JtYXRpb24gcGxlYXNlICh3aXRoIGNoZXJyeSBvbiB0b3ApISJ9LCJuYmYiOjE2MTY3MDY4OTQsImp0aSI6IjA5ZGY2OTA4LWJjMzAtNDU2Yi05MmU0LTFhNTI2NzBjNjllNyJ9.KPdRfAnMFZ_LjLAoBU-JrvVf7-R6L3d4c8A1htK5nyvixzBtRPEiY50B-98UXBPa8vhYDDRfIYUe6RGjseEsqg"

    @Test
    fun `test to get Presentation Request with linked domains unverified`() {
        val presentationOpenIdUrl = ""
        runBlocking {
            val actualRequest = presentationService.getRequest(presentationOpenIdUrl)
            Assertions.assertThat(actualRequest).isInstanceOf(Result.Success::class.java)
        }

    }
}