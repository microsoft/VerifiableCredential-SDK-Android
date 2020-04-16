// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.PostNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class SendIssuanceResponseNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider): PostNetworkOperation<IssuanceServiceResponse>() {
    override val call: suspend () -> Response<IssuanceServiceResponse> = { apiProvider.issuanceApis.sendResponse(url, serializedResponse) }
}