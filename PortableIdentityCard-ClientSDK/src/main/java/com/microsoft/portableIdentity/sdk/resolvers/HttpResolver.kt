package com.microsoft.portableIdentity.sdk.resolvers

import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.PortableIdentityRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Fetches Identifier Documents from remote resolvers over http.
 * @class
 * @implements IResolver
 * @param url of the remote resolver.
 */
@Singleton
class HttpResolver @Inject constructor(
    @Named("resolverUrl") private val baseUrl: String,
    private val identityRepository: PortableIdentityRepository
    ) : Resolver() {

    override suspend fun resolveDocument(identifier: String): IdentifierDocument {
        return identityRepository.resolveIdentifier(baseUrl, identifier)
    }
}