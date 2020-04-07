package com.microsoft.portableIdentity.sdk.resolvers

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument

/**
 * Interface defining methods and properties to
 * be implemented by specific resolver methods.
 * @interface
 */
abstract class Resolver {

    /**
     * Returns the identifier document for the specified
     * identifier.
     * @param identifier for which to return the identifier document.
     */
    abstract suspend fun resolveDocument(
        identifier: String
    ): IdentifierDocument

    suspend fun resolve(
        identifier: String, cryptoOperations: CryptoOperations
    ): Identifier {
        val document = this.resolveDocument(identifier)
        return Identifier(
            identifier,
            "",
            "",
            "",
            "",
            "",
            "",
            document,
            ""
        )
    }
}