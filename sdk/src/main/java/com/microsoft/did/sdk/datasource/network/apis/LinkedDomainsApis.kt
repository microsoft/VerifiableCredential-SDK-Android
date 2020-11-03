// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.datasource.network.apis

import com.microsoft.did.sdk.credential.service.models.serviceResponses.LinkedDomainsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface LinkedDomainsApis {

    @GET
    suspend fun fetchWellKnownConfigDocument(@Url overrideUrl: String): Response<LinkedDomainsResponse>
}