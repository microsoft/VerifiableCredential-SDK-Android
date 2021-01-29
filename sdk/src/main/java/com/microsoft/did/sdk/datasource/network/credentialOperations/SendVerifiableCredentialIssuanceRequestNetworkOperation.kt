/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.did.sdk.credential.service.validators.JwtValidator
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.InvalidSignatureException
import com.microsoft.did.sdk.util.controlflow.IssuanceException
import com.microsoft.did.sdk.util.controlflow.Result
import kotlinx.serialization.json.Json
import retrofit2.Response

class SendVerifiableCredentialIssuanceRequestNetworkOperation(
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider,
    private val jwtValidator: JwtValidator,
    private val serializer: Json
) : PostNetworkOperation<IssuanceServiceResponse, VerifiableCredential>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }

    override suspend fun onSuccess(response: Response<IssuanceServiceResponse>): Result<VerifiableCredential> {
        val jwsTokenString = response.body()?.vc ?: throw IssuanceException("No Verifiable Credential in Body.")
        return verifyAndUnWrapIssuanceResponse(jwsTokenString)
    }

    private suspend fun verifyAndUnWrapIssuanceResponse(jwsTokenString: String): Result<VerifiableCredential> {
        val jwsToken = JwsToken.deserialize(jwsTokenString)
        if (!jwtValidator.verifySignature(jwsToken))
            throw InvalidSignatureException("Signature is not Valid on Issuance Response.")
        val verifiableCredentialContent = serializer.decodeFromString(VerifiableCredentialContent.serializer(), jwsToken.content())
        return Result.Success(VerifiableCredential(verifiableCredentialContent.jti, jwsTokenString, verifiableCredentialContent))
    }
}