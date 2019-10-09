package com.microsoft.did.sdk.resolvers

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.registrars.IRegistrar
import com.microsoft.did.sdk.registrars.NullRegistrar

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
    open suspend fun resolveDocument(identifier: String): IdentifierDocument

    suspend fun resolve(identifier: String,
                cryptoOperations: CryptoOperations): Identifier {
        val document = this.resolveDocument(identifier)
        val encKey = document.publicKeys.filter{
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
            cryptoOperations,
            this,
            NullRegistrar()
        )
    }
}