// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.apis

import com.microsoft.did.sdk.credential.service.models.serviceResponses.DnsBindingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface DnsBindingApis {

    @GET
    suspend fun getWellKnownConfigDocument(@Url overrideUrl: String): Response<DnsBindingResponse>
}