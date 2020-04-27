/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking.cardOperations

import com.microsoft.portableIdentity.sdk.repository.networking.PostNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import retrofit2.Response

class SendPresentationResponseNetworkOperation(url: String, serializedResponse: String, apiProvider: ApiProvider): PostNetworkOperation<String, Unit>() {
    override val call: suspend () -> Response<String> = { apiProvider.presentationApis.sendResponse(url, serializedResponse) }

    override fun onSuccess(response: Response<String>): Result<Unit> {
        return Result.Success(Unit)
    }
}