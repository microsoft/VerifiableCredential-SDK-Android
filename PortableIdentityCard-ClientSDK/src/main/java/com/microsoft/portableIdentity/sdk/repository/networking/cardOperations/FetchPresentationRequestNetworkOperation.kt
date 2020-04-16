// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.repository.networking.GetNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class FetchPresentationRequestNetworkOperation(url: String, apiProvider: ApiProvider): GetNetworkOperation<String>() {
    override val call: suspend () -> Response<String> = { apiProvider.presentationApis.getRequest(url) }
}