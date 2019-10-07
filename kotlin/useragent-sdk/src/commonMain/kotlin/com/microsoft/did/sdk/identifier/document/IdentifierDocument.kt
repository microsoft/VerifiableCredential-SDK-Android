package com.microsoft.did.sdk.identifier.document

import com.microsoft.did.sdk.identifier.IdentifierDocumentService
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdentifierDocument(val id: String, val publicKeys: List<IdentifierDocumentPublicKey>, val services: List<IdentifierDocumentService>) {
}