package com.microsoft.portableIdentity.sdk.resolvers

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.identifier.IdentifierResponse
import com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.registrars.NullRegistrar
import com.microsoft.portableIdentity.sdk.utilities.ILogger

/**
 * Interface defining methods and properties to
 * be implemented by specific resolver methods.
 * @interface
 */
abstract class IResolver(internal val logger: ILogger) {

    /**
     * Returns the identifier document for the specified
     * identifier.
     * @param identifier for which to return the identifier document.
     */
    abstract suspend fun resolveDocument(identifier: String): IdentifierDocument

    abstract suspend fun resolveDocument(identifier: String, initialValues: String): com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument

    suspend fun resolve(
        identifier: String,
        cryptoOperations: CryptoOperations
    ): Identifier {
        val document = this.resolveDocument(identifier)
        val encKey = document.publicKeys.filter {
            if (it.publicKeyJwk.use != null) {
                it.publicKeyJwk.use == KeyUse.Encryption.value
            } else if (it.publicKeyJwk.key_ops != null) {
                it.publicKeyJwk.key_ops!!.contains(KeyUsage.Encrypt.value)
            } else {
                false
            }
        }.firstOrNull()?.publicKeyJwk?.kid ?: ""
        return Identifier(
            document,
            "",
            encKey,
            "",
            cryptoOperations,
            logger,
            this,
            NullRegistrar(logger)
        )
    }

    suspend fun resolve(
        identifier: String, initialValues: String,
        cryptoOperations: CryptoOperations
    ): IdentifierResponse {
        val document = this.resolveDocument(identifier, initialValues)
        return IdentifierResponse(
            document,
            "",
            "",
            "",
            cryptoOperations
        )
    }
}