/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository.networking.apis

import retrofit2.Response
import retrofit2.http.*

interface PresentationApis {

    @GET
    suspend fun getRequest(@Url overrideUrl: String): Response<String>

    @FormUrlEncoded
    @POST
    suspend fun sendResponse(@Url overrideUrl: String, @Field("id_token") token: String): Response<String>
}