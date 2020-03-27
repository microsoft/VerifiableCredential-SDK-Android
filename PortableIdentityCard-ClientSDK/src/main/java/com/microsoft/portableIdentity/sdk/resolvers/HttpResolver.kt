package com.microsoft.portableIdentity.sdk.resolvers

import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.utilities.*
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.serialization.Serializable
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
    @Named("resolverUrl") private val baseUrl: String, logger: Logger
) : IResolver(logger) {

    @Serializable
    data class ResolverMetadata(val driverId: String?, val driver: String?, val retrieved: String?, val duration: String?)

    @Serializable
    data class ResolverResult(val document: IdentifierDocument, val resolverMetadata: ResolverMetadata)

    /**
     * Sends a fetch request to the resolver URL
     * to resolver specified Identifier
     * @param identifier to resolve
     */
    override suspend fun resolveDocument(identifier: String): IdentifierDocument {
        val client = getHttpClient()
        val response = client.get<String>() {
            url("$baseUrl/$identifier ")
        }
        client.close()
        println("GOT $response")
        val result = Serializer.parse(ResolverResult.serializer(), response)
        println("resolved ${result.document.id} with metadata ${result.resolverMetadata}")
        return result.document
    }
}