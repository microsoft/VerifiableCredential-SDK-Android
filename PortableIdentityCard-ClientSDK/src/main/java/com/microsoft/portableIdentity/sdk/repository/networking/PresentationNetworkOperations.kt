/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.PresentationServiceResponse
import com.microsoft.portableIdentity.sdk.repository.networking.apis.PortableIdentityCardApi
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationNetworkOperations @Inject constructor(retrofit: Retrofit): HttpBaseOperation() {

    private val picApi: PortableIdentityCardApi = retrofit.create(PortableIdentityCardApi::class.java)

    /**
     * Get Request from url.
     */
    suspend fun fetchRequestToken(url: String): HttpResult<String, String, Exception> {
        return fire(call = {picApi.getRequest(url)}, parseError = parseGenericErrorBody)
    }

    /**
     * Post Presentation Response to url.
     */
    suspend fun sendResponse(url: String, serializedResponse: String): HttpResult<PresentationServiceResponse, String, Exception> {
        return fire(call = {picApi.sendPresentationResponse(url, serializedResponse)}, parseError = parseGenericErrorBody)
    }
}