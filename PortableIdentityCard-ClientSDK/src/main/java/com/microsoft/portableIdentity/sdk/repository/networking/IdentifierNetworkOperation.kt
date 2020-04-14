package com.microsoft.portableIdentity.sdk.repository.networking

import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.apis.IdentifierApi
import com.microsoft.portableIdentity.sdk.utilities.controlflow.ResolverException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import retrofit2.Retrofit
import javax.inject.Inject

class IdentifierNetworkOperation @Inject constructor(retrofit: Retrofit): HttpBaseOperation() {

    private val identifierApi: IdentifierApi = retrofit.create(IdentifierApi::class.java)

    suspend fun resolveIdentifier(url: String, identifier: String): Result<IdentifierDocument, Exception> {
        val resolverUrl = "$url/$identifier"
/*        val identifierDocument = fire(
            call = {identifierApi.resolveIdentifier(resolverUrl)},
            errorMessage = "Error resolving identifier $identifier from $url."
        ) ?: return Result.Failure(ResolverException("Error resolving identifier $identifier from $url"))
        return Result.Success(identifierDocument)*/
        val identifierDocument = fire(
            call = {identifierApi.resolveIdentifier(resolverUrl)},
            errorMessage = "Error resolving identifier $identifier from $url."
        )
        return when (identifierDocument) {
            null -> Result.Failure(ResolverException("Error resolving identifier $identifier from $url"))
            else -> return Result.Success(identifierDocument)
        }
    }
}