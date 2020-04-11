package com.microsoft.portableIdentity.sdk.identifier.models.document

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
data class IdentifierDocument(
    val publicKey: List<IdentifierDocumentPublicKey>,
    val service: List<IdentifierDocumentService>?,
    val recoveryKey: JsonWebKey,
    val id: String
) {
}