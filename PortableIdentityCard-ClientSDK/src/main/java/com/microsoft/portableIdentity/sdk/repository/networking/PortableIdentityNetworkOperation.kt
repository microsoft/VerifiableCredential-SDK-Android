package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.apis.PortableIdentityApi
import retrofit2.Retrofit
import javax.inject.Inject

class PortableIdentityNetworkOperation @Inject constructor(retrofit: Retrofit): HttpBaseOperation() {

    private val identityApi: PortableIdentityApi = retrofit.create(PortableIdentityApi::class.java)

    suspend fun resolveIdentifier(url: String, identifier: String): IdentifierDocument? {
        return fire(
            call = {identityApi.resolveIdentifier("$url/$identifier")},
            errorMessage = "Error resolving identifier from $url."
        )
    }
}