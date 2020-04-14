package com.microsoft.portableIdentity.sdk.resolvers

import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import javax.inject.Inject
import javax.inject.Named

class Resolver @Inject constructor(@Named("resolverUrl") private val baseUrl: String, private val identifierRepository: IdentifierRepository) {
    suspend fun resolve(identifier: String): Result<IdentifierDocument, Exception> {
        return identifierRepository.resolveIdentifier(baseUrl, identifier)
    }
}