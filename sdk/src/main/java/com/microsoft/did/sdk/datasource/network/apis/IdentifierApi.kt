/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.network.apis

import com.microsoft.did.sdk.identifier.models.identifierdocument.IdentifierResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface IdentifierApi {

    @GET
    suspend fun resolveIdentifier(@Url overrideUrl: String): Response<IdentifierResponse>
}