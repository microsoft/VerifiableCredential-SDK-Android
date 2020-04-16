package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class SendIssuanceResponseNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider): PostNetworkOperation<IssuanceServiceResponse>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.picApi.sendIssuanceResponse(url, serializedResponse) }
}