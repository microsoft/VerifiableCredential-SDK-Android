/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceServiceError
import com.microsoft.portableIdentity.sdk.repository.networking.apis.PortableIdentityCardApi
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.IssuanceException
import okhttp3.ResponseBody
import retrofit2.Retrofit
import javax.inject.Inject

class IssuanceNetworkOperations @Inject constructor(retrofit: Retrofit): HttpBaseOperation()  {

    private val picApi: PortableIdentityCardApi = retrofit.create(PortableIdentityCardApi::class.java)

    /**
     * Get Contract from url.
     */
    suspend fun fetchContract(url: String): HttpResult<PicContract, String, Exception> {
        return fire(call = {picApi.getContract(url)}, parseError = parseGenericErrorBody)
    }

    /**
     * Post Response to url.
     */
    suspend fun sendResponse(url: String, serializedResponse: String): HttpResult<IssuanceServiceResponse, IssuanceServiceError, Exception> {
        val parseIssuanceError = { body: ResponseBody? -> parseError(body)}
        return fire(call = {picApi.sendIssuanceResponse(url, serializedResponse)}, parseError = parseIssuanceError)
    }

    private fun parseError(body: ResponseBody?): IssuanceServiceError {
        val serializedBody = body?.string() ?: throw IssuanceException("Error Body is null")
        val serviceError = Serializer.parse(IssuanceServiceError.serializer(), serializedBody)
        return serviceError
    }
}