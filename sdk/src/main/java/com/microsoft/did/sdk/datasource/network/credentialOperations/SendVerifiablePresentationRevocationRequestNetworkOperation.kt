// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.RevocationResponse
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
) : PostNetworkOperation<RevocationResponse, String>() {
    override val call: suspend () -> Response<RevocationResponse> = { apiProvider.revocationApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<RevocationResponse>): Result<String> {
        val credentialStatus = response.body()?.credentialStatus ?: throw SdkException("No Verifiable Credential in Body.")
        return Result.Success(credentialStatus)
    }
}