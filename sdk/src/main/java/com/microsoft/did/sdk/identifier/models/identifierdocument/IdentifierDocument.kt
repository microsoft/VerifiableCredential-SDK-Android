package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.identifier.models.payload.document.IdentifierDocumentService
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Document returned on resolving an identifier
 * Refer to https://www.w3.org/TR/did-core/#core-properties for more details on identifier document
 */
@Serializable
data class IdentifierDocument(
    val publicKey: List<IdentifierDocumentPublicKey>,
    val id: String
) {
    var service: List<IdentifierDocumentService> = emptyList()
}