/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.oidc.PresentationRequestContent
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.DidInHeaderAndPayloadNotMatching
import com.microsoft.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.did.sdk.util.controlflow.PresentationException
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.json.Json
import retrofit2.Response

//TODO("improve onSuccess method to create receipt when this is spec'd out")
class FetchPresentationRequestNetworkOperation(
    private val url: String,
    private val apiProvider: ApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : GetNetworkOperation<String, PresentationRequestContent>() {
    override val call: suspend () -> Response<String> = { apiProvider.presentationApis.getRequest(url) }

    override suspend fun onSuccess(response: Response<String>): Result<PresentationRequestContent> {
        val jwsTokenString = response.body() ?: throw PresentationException("No Presentation Request in Body.")
        return verifyAndUnwrapPresentationRequest(jwsTokenString)
    }

    private suspend fun verifyAndUnwrapPresentationRequest(jwsTokenString: String): Result<PresentationRequestContent> {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        val siop = """{
  "response_type": "id_token",
  "response_mode": "post",
  "client_id": "https://test-relyingparty.azurewebsites.net/verify",
  "redirect_uri": "https://test-relyingparty.azurewebsites.net/verify",
  "scope": "openid",
  "state": "7rknJUsjoNKVNQ",
  "nonce": "NtmVFJAOnL_nqQ",
  "iss": "did:ion:EiAgGyo01Zdhz0LuyqKkdkoGq2u1L7shLREQWRp-KRExvw:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJzaWduIiwicHVibGljS2V5SndrIjp7ImNydiI6InNlY3AyNTZrMSIsImt0eSI6IkVDIiwieCI6IlZ3bW9ja1RkNWswT2VWWjNVZHVmWEE3NjUtbVFVVVR2Mm5WX1hqZHZ4SjAiLCJ5IjoiMUNCOXh5aTdVLWMwUHF0UHFLYW92Z3VxakdJajZMcERIMFNnSDg3Z1dZRSJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiIsImtleUFncmVlbWVudCJdLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5In1dLCJzZXJ2aWNlcyI6W119fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUJRSGJWeEhYa3NONEJvOHprbHdxSnFKSFM2R1dZOXQ2YnFCaGdzVjNwbFNnIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlDMlRhejQxVW5WYmdTQ1Ezcml3U2oyR1NDenF1Mkx4UWhDUF8yZU5aNUxGdyIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQzl4cENtVVZrWWFwNFdXNVc5ZnhxbjkyaUR1ekpqLVJIUnN1QXlzMFBEX1EifX0",
  "registration": {
    "client_name": "Decentralized Identity Team",
    "client_purpose": "Give us this information please (with cherry on top)!",
    "tos_uri": "https://test-relyingparty.azurewebsites.net/tos.html",
    "logo_uri": "https://test-relyingparty.azurewebsites.net/images/did_logo.png"
  },
  "iat": 1634050645,
  "exp": 1734050945,
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
        "constraints": {
          "fields": [
            {
              "path": [
                "$.issuer",
                "$.vc.issuer",
                "$.iss"
              ],
              "purpose": "We can only verify bank accounts if they are attested by a trusted bank, auditor or regulatory authority.",
              "filter": {
                "type": "string",
                "pattern": "did:ion:EiAIA7uUm7QOD42265BKYctPjwZMgPMrH2w6TELh2TvLvQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJmZjFlOTE2NzBhZDc0YWM5YmU0ODFkODExZjkxNjM0MSIsInB1YmxpY0tleUp3ayI6eyJjcnYiOiJzZWNwMjU2azEiLCJraWQiOiJmZjFlOTE2NzBhZDc0YWM5YmU0ODFkODExZjkxNjM0MSIsImt0eSI6IkVDIiwidXNlIjoic2lnIiwieCI6IklnbTRVRGVTb0duVFRRTWhuSUtXLVQtR0lFRzUxV0JUUFVhc0FGVE1scnciLCJ5IjoiVjRzYXRlNjU1cV9FZ2VaTDRCSHI5dlM4VEdldmVoNjJQa0kyR0NzN3F4TSJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiRWNkc2FTZWNwMjU2azFWZXJpZmljYXRpb25LZXkyMDE5In1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlCaHFkNEt6UUZtN1REai1hcjgyQU1QN3UyWFhTTmsyVTVmaGttcjRWNFVSZyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQXFnVjhCSjR5THJ6WGlYMHdEUVd1TDNJT3NHWUtQaTBBcGJrUG9USmpwTmciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUFRanNnQWVqcEstZnl2ZTFIVWRVM2RyaHhZdGpJQ2c1cDNDNFR1Y3hZeUtRIn19"              }
            }
	      ]
        }
      }
    ],
    "name": "Decentralized Identity Team",
    "purpose": "Give us this information please (with cherry on top)!"
  },
  "nbf": 1634050645,
  "jti": "38adbdee-d234-4bd2-a13b-5593ee5a0fcf"
}"""
        val presentationRequestContent = serializer.decodeFromString(PresentationRequestContent.serializer(), siop)
/*        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not valid on Presentation Request.")
        if (!jwtValidator.validateDidInHeaderAndPayload(jwsToken, presentationRequestContent.issuer))
            throw DidInHeaderAndPayloadNotMatching("DID used to sign the presentation request doesn't match the DID in presentation request.")*/
        return Result.Success(presentationRequestContent)
    }
}