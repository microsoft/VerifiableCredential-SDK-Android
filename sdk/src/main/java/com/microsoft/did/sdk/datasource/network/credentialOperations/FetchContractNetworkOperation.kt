/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.ContractServiceResponse
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.IssuanceException
import com.microsoft.did.sdk.util.controlflow.Result
import retrofit2.Response

class FetchContractNetworkOperation(val url: String, apiProvider: ApiProvider) :
    GetNetworkOperation<ContractServiceResponse, String>() {
    override val call: suspend () -> Response<ContractServiceResponse> = { apiProvider.issuanceApis.getContract(url) }

    override fun onSuccess(response: Response<ContractServiceResponse>): Result<String> {
        val contract = response.body()?.token ?: throw IssuanceException("Contract was not found in response")
        return Result.Success(contract)
    }
}