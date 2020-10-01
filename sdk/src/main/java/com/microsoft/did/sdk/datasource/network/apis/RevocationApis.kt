// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.apis

import com.microsoft.did.sdk.credential.service.models.serviceResponses.RevocationServiceResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface RevocationApis {

    @POST
    suspend fun sendResponse(@Url overrideUrl: String, @Body body: String): Response<RevocationServiceResponse>
}