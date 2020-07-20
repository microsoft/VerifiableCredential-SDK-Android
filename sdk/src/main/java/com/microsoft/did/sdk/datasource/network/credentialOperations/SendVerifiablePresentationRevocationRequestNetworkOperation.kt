// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import com.microsoft.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.SdkException
import com.microsoft.did.sdk.util.serializer.Serializer
import retrofit2.Response

class SendVerifiablePresentationRevocationRequestNetworkOperation (
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider,
    private val serializer: Serializer
) : PostNetworkOperation<RevocationServiceResponse, String>() {
    override val call: suspend () -> Response<RevocationServiceResponse> = { apiProvider.revocationApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(serviceResponse: Response<RevocationServiceResponse>): Result<String> {
        val receipts = serviceResponse.body()?.receipt?.entries
        val serializedReceipt = receipts?.first()?.value ?: throw SdkException("No Receipt in response body.")
        return Result.Success(serializedReceipt)
    }
}