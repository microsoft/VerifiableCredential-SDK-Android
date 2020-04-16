package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class FetchContractNetworkOperation(val url: String, apiProvider: ApiProvider): GetNetworkOperation<PicContract>() {
    override val call: suspend () -> Response<PicContract> = { apiProvider.picApi.getContract(url) }
}