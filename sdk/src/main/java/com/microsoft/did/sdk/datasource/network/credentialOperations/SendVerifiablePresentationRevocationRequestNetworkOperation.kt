/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.credentialOperations

import com.microsoft.did.sdk.credential.models.RevocationReceipt
import com.microsoft.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.datasource.network.PostNetworkOperation
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.RevocationException
import kotlinx.serialization.json.Json
import retrofit2.Response

class SendVerifiablePresentationRevocationRequestNetworkOperation(
    url: String,
    serializedResponse: String,
    apiProvider: ApiProvider,
    private val serializer: Json
) : PostNetworkOperation<RevocationServiceResponse, RevocationReceipt>() {
    override val call: suspend () -> Response<RevocationServiceResponse> =
        { apiProvider.revocationApis.sendResponse(url, serializedResponse) }

    override suspend fun onSuccess(response: Response<RevocationServiceResponse>): Result<RevocationReceipt> {
        val receipts = response.body()?.receipt?.entries
        if (receipts == null || receipts.isEmpty())
            throw RevocationException("No Receipt in revocation response body")
        val serializedReceipt = receipts.first().value
        val revocationReceipt = unwrapRevocationReceipt(serializedReceipt, serializer)
        return Result.Success(revocationReceipt)
    }

    fun unwrapRevocationReceipt(signedReceipt: String, serializer: Json): RevocationReceipt {
        val token = JwsToken.deserialize(signedReceipt)
        return serializer.decodeFromString(RevocationReceipt.serializer(), token.content())
    }
}