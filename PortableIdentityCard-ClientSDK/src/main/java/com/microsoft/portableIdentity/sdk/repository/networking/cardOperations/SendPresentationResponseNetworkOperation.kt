// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.PresentationServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.PostNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response

class SendPresentationResponseNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider): PostNetworkOperation<PresentationServiceResponse>() {
    override val call: suspend () -> Response<PresentationServiceResponse> = { apiProvider.presentationApis.sendResponse(url, serializedResponse) }
}