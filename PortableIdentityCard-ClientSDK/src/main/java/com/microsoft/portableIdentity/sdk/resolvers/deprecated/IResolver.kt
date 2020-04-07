// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.resolvers.deprecated

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.KeyUse
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.registrars.deprecated.NullRegistrar

/**
 * Interface defining methods and properties to
 * be implemented by specific resolver methods.
 * @interface
 */
abstract class IResolver {

    /**
     * Returns the identifier document for the specified
     * identifier.
     * @param identifier for which to return the identifier document.
     */
    abstract suspend fun resolveDocument(identifier: String): IdentifierDocument

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
            this,
            NullRegistrar()
        )
    }
}