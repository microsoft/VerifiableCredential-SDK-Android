package com.microsoft.did.sdk.identifier

class Identifier {

    private val document: IdentifierDocument

    /**
     * Initialize Identifier with Identifier Document.
     * TODO: add options.
     */
    constructor(document: IdentifierDocument) {
        this.document = document
    }

    /**
     * Initialize Identifier with Identifier string.
     * TODO: add options.
     */
    constructor(identifier: String) {
        this.document = this.getDocument(identifier);
    }

    /**
     * Fetch Identifier Document from the Discovery Service.
     */
    fun getDocument(identifier: String): IdentifierDocument {
        throw Error("Not Implemented")
    }
}