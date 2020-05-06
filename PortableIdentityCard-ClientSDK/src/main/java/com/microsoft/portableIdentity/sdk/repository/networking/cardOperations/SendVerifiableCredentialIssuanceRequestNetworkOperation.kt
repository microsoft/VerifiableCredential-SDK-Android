/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceServiceError
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.repository.networking.PostNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.IssuanceException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import retrofit2.Response

class SendVerifiableCredentialIssuanceRequestNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider, private val serializer: Serializer): PostNetworkOperation<IssuanceServiceResponse, VerifiableCredential>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<IssuanceServiceResponse>): Result<VerifiableCredential> {
        val signedVerifiableCredential = response.body()?.vc ?: throw IssuanceException("No Verifiable Credential in Body.")
        val contents = unwrapSignedVerifiableCredential(signedVerifiableCredential)
        return Result.Success(VerifiableCredential(signedVerifiableCredential, contents, contents.sub, contents.jti))
    }

    private fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String): VerifiableCredentialContent {
        val token = JwsToken.deserialize(signedVerifiableCredential, serializer)
        return serializer.parse(VerifiableCredentialContent.serializer(), token.content())
    }

    override fun onFailure(response: Response<IssuanceServiceResponse>): Result<Nothing> {
        val serializedBody = response.errorBody()?.string() ?: throw IssuanceException("Error Body is null")
        val serviceError = serializer.parse(IssuanceServiceError.serializer(), serializedBody)
        return Result.Failure(IssuanceException("Server Error: ${serviceError.code} with status code ${serviceError.httpStatusCode}"))
    }
}