/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.dnsBindingOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import retrofit2.Response
import javax.inject.Inject

class FetchWellKnownConfigDocumentNetworkOperation @Inject constructor(url: String, apiProvider: ApiProvider) :
    GetNetworkOperation<DnsBindingResponse, DnsBindingResponse>() {

    override val call: suspend () -> Response<DnsBindingResponse> =
        { apiProvider.dnsBindingApis.getWellKnownConfigDocument("$url/.well-known/did-configuration.json") }
}