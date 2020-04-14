/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking.apis

import com.microsoft.portableIdentity.sdk.auth.models.contracts.PicContract
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.IssuanceServiceResponse
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.PresentationServiceResponse
import com.microsoft.portableIdentity.sdk.auth.models.serviceResponses.ServiceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface PortableIdentityCardApi {

    @GET
    suspend fun getContract(@Url overrideUrl: String): Response<PicContract>

    @GET
    suspend fun getRequest(@Url overrideUrl: String): Response<String>

    @POST
    suspend fun sendResponse(@Url overrideUrl: String, @Body body: String): Response<IssuanceServiceResponse>

    @FormUrlEncoded
    @POST
    suspend fun sendPresentationResponse(@Url overrideUrl: String, @Field("id_token") token: String): Response<PresentationServiceResponse>
}