/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.repository.networking.cardOperations

import com.microsoft.did.sdk.auth.models.contracts.PicContract
import com.microsoft.did.sdk.repository.networking.GetNetworkOperation
import com.microsoft.did.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class FetchContractNetworkOperation(val url: String, apiProvider: ApiProvider) : GetNetworkOperation<PicContract, PicContract>() {
    override val call: suspend () -> Response<PicContract> = { apiProvider.issuanceApis.getContract(url) }
}