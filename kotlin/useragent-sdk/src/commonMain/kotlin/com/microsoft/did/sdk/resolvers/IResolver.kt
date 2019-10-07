package com.microsoft.did.sdk.resolvers

import com.microsoft.did.sdk.identifier.document.IdentifierDocument

/**
 * Interface defining methods and properties to
 * be implemented by specific resolver methods.
 * @interface
 */
interface IResolver {

    /**
     * Returns the identifier document for the specified
     * identifier.
     * @param identifier for which to return the identifier document.
     */
    open fun resolve(identifier: String): IdentifierDocument
}