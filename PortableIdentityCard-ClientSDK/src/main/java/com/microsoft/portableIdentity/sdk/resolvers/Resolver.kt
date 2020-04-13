package com.microsoft.portableIdentity.sdk.resolvers

import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
import javax.inject.Inject
import javax.inject.Named

class Resolver @Inject constructor(@Named("resolverUrl") private val baseUrl: String, private val identifierRepository: IdentifierRepository) {
    suspend fun resolve(identifier: String): IdentifierDocument {
        return identifierRepository.resolveIdentifier(baseUrl, identifier)
    }
}