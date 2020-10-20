/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.IssuanceException
import com.microsoft.did.sdk.util.controlflow.Result
import retrofit2.Response

open class SendVerifiableCredentialIssuanceRequestNetworkOperation(
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider
) : PostNetworkOperation<IssuanceServiceResponse, String>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<IssuanceServiceResponse>): Result<String> {
        val rawVerifiableCredential = response.body()?.vc ?: throw IssuanceException("No Verifiable Credential in Body.")
        return Result.Success(rawVerifiableCredential)
    }
}