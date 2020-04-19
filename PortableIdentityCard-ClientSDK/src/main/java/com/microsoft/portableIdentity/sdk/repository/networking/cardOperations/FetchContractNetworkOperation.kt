// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.repository.networking.GetNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class FetchContractNetworkOperation(val url: String, apiProvider: ApiProvider): GetNetworkOperation<PicContract, PicContract>() {
    override val call: suspend () -> Response<PicContract> = { apiProvider.issuanceApis.getContract(url) }
}