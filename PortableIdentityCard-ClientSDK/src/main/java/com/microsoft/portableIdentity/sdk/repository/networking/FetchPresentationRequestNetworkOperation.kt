package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class FetchPresentationRequestNetworkOperation(url: String, apiProvider: ApiProvider): GetNetworkOperation<String>() {
    override val call: suspend () -> Response<String> = { apiProvider.picApi.getRequest(url) }
}