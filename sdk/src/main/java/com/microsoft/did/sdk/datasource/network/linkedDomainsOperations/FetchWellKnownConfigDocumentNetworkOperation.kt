/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.linkedDomainsOperations

import com.microsoft.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import com.microsoft.did.sdk.datasource.network.GetNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.Constants
import retrofit2.Response
import java.net.URL
import javax.inject.Inject

class FetchWellKnownConfigDocumentNetworkOperation @Inject constructor(val url: String, apiProvider: ApiProvider) :
    GetNetworkOperation<LinkedDomainsResponse, LinkedDomainsResponse>() {

    override val call: suspend () -> Response<LinkedDomainsResponse> =
        {
            val contextPath = URL(url)
            apiProvider.linkedDomainsApis.fetchWellKnownConfigDocument(
                URL(
                    contextPath,
                    Constants.WELL_KNOWN_CONFIG_DOCUMENT_LOCATION
                ).toURI()
            )
        }
}