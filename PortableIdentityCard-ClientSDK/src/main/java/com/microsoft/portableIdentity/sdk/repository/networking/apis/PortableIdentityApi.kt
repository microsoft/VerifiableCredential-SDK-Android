package com.microsoft.portableIdentity.sdk.repository.networking.apis

import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface PortableIdentityApi {

    @GET
    suspend fun resolveIdentifier(@Url overrideUrl: String): Response<IdentifierDocument>
}