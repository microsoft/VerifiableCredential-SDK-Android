/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking.identifierOperations

import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.GetNetworkOperation
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import retrofit2.Response
import javax.inject.Inject

class ResolveIdentifierNetworkOperation @Inject constructor(apiProvider: ApiProvider, url: String, val identifier: String):
    GetNetworkOperation<IdentifierDocument, IdentifierDocument>() {

    override val call: suspend() -> Response<IdentifierDocument> = {apiProvider.identifierApi.resolveIdentifier("$url/$identifier")}
}