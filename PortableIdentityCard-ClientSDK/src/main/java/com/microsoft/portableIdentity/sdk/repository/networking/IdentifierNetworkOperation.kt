package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.apis.IdentifierApi
import retrofit2.Retrofit
import javax.inject.Inject

class IdentifierNetworkOperation @Inject constructor(retrofit: Retrofit): HttpBaseOperation() {

    private val identifierApi: IdentifierApi = retrofit.create(IdentifierApi::class.java)

    suspend fun resolveIdentifier(url: String, identifier: String): IdentifierDocument? {
        val resolverUrl = "$url/$identifier"
        return fire(
            call = {identifierApi.resolveIdentifier(resolverUrl)},
            errorMessage = "Error resolving identifier $identifier from $url."
        )
    }
}