package com.microsoft.did.sdk.resolvers

import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.utilities.*
import io.ktor.client.request.get
import io.ktor.client.request.url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Fetches Identifier Documents from remote resolvers over http.
 * @class
 * @implements IResolver
 * @param url of the remote resolver.
 */
class HttpResolver(private val baseUrl : String, logger: ILogger): IResolver(logger) {

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
        val polymorphicSerialization: IPolymorphicSerialization = PolymorphicSerialization
        val result = polymorphicSerialization.parse(ResolverResult.serializer(), response)
//        val result = MinimalJson.serializer.parse(ResolverResult.serializer(), response)
        println("resolved ${result.document.id} with metadata ${result.resolverMetadata}")
        return result.document
    }
}