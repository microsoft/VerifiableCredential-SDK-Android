package com.microsoft.did.sdk.identifier

import kotlinx.serialization.Serializable

@Serializable
data class Document(val id: String, val publicKeys: ArrayList<IdentifierDocumentPublicKey>) {}

/**
 * Class to represent Identifier Documents.
 * @class
 */
class IdentifierDocument(document: Document) {

    /**
     * Creates an Identifier Document from an id and publicKeys.
     */
    constructor(id: String, publicKeys: ArrayList<IdentifierDocumentPublicKey>): this(Document(id, publicKeys))
}