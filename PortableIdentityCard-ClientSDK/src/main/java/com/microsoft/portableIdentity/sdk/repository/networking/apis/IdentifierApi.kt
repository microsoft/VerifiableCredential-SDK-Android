package com.microsoft.portableIdentity.sdk.repository.networking.apis

import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface IdentifierApi {

    @GET
    suspend fun resolveIdentifier(@Url overrideUrl: String): Response<IdentifierDocument>
}