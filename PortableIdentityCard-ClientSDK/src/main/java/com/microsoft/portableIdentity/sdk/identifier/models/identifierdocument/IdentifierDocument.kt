package com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.models.payload.document.IdentifierDocumentService
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