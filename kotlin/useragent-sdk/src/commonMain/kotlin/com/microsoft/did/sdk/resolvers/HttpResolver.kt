package com.microsoft.did.sdk.resolvers

import com.microsoft.did.sdk.IResolver
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierDocument

/**
 * Fetches Identifier Documents from remote resolvers over http.
 * @class
 * @implements IResolver
 * @param url of the remote resolver.
 */
class HttpResolver(url : String): IResolver {

    /**
     * Sends a fetch request to the resolver URL
     * to resolver specified Identifier
     * @param identifier to resolve
     */
    override fun resolve(identifier: String): IdentifierDocument {
        TODO("not implemented")
    }
}