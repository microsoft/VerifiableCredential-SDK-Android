/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.contracts.VcContract
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import retrofit2.Response

class FetchContractNetworkOperation(val url: String, apiProvider: ApiProvider) : GetNetworkOperation<VcContract, VcContract>() {
    override val call: suspend () -> Response<VcContract> = { apiProvider.issuanceApis.getContract(url) }
}