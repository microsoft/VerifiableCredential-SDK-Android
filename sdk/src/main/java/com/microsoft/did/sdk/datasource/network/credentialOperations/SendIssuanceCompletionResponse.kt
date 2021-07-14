/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.Result
import retrofit2.Response

class SendIssuanceCompletionResponse(
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider
) : PostNetworkOperation<Unit, Unit>() {
    override val call: suspend () -> Response<Unit> = { apiProvider.issuanceApis.sendCompletionResponse(url, serializedResponse) }

    override suspend fun onSuccess(response: Response<Unit>): Result<Unit> {
        return Result.Success(Unit)
    }
}