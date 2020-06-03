/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.repository.networking.cardOperations

import com.microsoft.did.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.did.sdk.repository.networking.PostNetworkOperation
import com.microsoft.did.sdk.repository.networking.apis.ApiProvider
import com.microsoft.did.sdk.utilities.Serializer
import com.microsoft.did.sdk.utilities.controlflow.IssuanceException
import com.microsoft.did.sdk.utilities.controlflow.Result
import retrofit2.Response

class SendVerifiableCredentialIssuanceRequestNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider, private val serializer: Serializer): PostNetworkOperation<IssuanceServiceResponse, String>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<IssuanceServiceResponse>): Result<String> {
        val rawVerifiableCredential = response.body()?.vc ?: throw IssuanceException("No Verifiable Credential in Body.")
        return Result.Success(rawVerifiableCredential)
    }
}