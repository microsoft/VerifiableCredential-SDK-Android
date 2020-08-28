/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.contracts.VerifiableCredentialContract
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.IssuanceException
import com.microsoft.did.sdk.util.controlflow.Result
import retrofit2.Response

class FetchContractNetworkOperation(val url: String, apiProvider: ApiProvider) :
    GetNetworkOperation<VerifiableCredentialContract, VerifiableCredentialContract>() {
    override val call: suspend () -> Response<VerifiableCredentialContract> = { apiProvider.issuanceApis.getContract(url) }

    override fun onSuccess(response: Response<VerifiableCredentialContract>): Result<VerifiableCredentialContract> {
        val contract = response.body() ?: throw IssuanceException("Contract was not found in response")
        contract.input.attestations.idTokens.map { claim -> claim.claims.map { type -> if (type.type == null) type.type = "" } }
        return Result.Success(contract)
    }
}