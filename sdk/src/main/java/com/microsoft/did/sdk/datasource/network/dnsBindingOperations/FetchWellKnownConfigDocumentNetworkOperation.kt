/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.dnsBindingOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.Constants
import retrofit2.Response
import javax.inject.Inject

class FetchWellKnownConfigDocumentNetworkOperation @Inject constructor(val url: String, apiProvider: ApiProvider) :
    GetNetworkOperation<DnsBindingResponse, DnsBindingResponse>() {

    override val call: suspend () -> Response<DnsBindingResponse> =
        { apiProvider.dnsBindingApis.fetchWellKnownConfigDocument("$url/${Constants.WELL_KNOWN_CONFIG_DOCUMENT_LOCATION}") }
}