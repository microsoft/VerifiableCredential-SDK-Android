package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.document.*

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class IdentifierResponse constructor(
    val document: IdentifierDocument,
    var signatureKeyReference: String,
    val encryptionKeyReference: String,
    var alias: String,
    private val cryptoOperations: CryptoOperations
    ) {

    fun serialize(): String {
        return IdentifierResponseToken.serialize(this)
    }
}
