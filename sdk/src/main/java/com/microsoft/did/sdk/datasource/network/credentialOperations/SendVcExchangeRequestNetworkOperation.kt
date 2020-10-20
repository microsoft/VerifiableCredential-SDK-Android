/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.datasource.network.apis.ApiProvider

class SendVcExchangeRequestNetworkOperation(
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider
) : SendVerifiableCredentialIssuanceRequestNetworkOperation(url, serializedResponse, apiProvider)