/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.PostNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.IssuanceException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import retrofit2.Response

class SendVerifiableCredentialIssuanceRequestNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider, private val serializer: Serializer): PostNetworkOperation<IssuanceServiceResponse, String>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<IssuanceServiceResponse>): Result<String> {
        val rawVerifiableCredential = response.body()?.vc ?: throw IssuanceException("No Verifiable Credential in Body.")
        return Result.Success(rawVerifiableCredential)
    }
}