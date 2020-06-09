/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import retrofit2.Response

//TODO("override onSuccess method to create receipt when this is spec'd out")
class FetchPresentationRequestNetworkOperation(url: String, apiProvider: ApiProvider) : GetNetworkOperation<String, String>() {
    override val call: suspend () -> Response<String> = { apiProvider.presentationApis.getRequest(url) }
}