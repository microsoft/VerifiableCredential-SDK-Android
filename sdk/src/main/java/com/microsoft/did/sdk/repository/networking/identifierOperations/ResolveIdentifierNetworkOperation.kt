/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.repository.networking.identifierOperations

import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import com.microsoft.did.sdk.repository.networking.GetNetworkOperation
import com.microsoft.did.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response
import javax.inject.Inject

class ResolveIdentifierNetworkOperation @Inject constructor(apiProvider: ApiProvider, url: String, val identifier: String):
    GetNetworkOperation<IdentifierResponse, IdentifierResponse>() {

    override val call: suspend() -> Response<IdentifierResponse> = {apiProvider.identifierApi.resolveIdentifier("$url/$identifier")}
}