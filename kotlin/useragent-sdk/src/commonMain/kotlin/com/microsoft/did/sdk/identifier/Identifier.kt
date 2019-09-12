package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.IResolver
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.registrars.IRegistrar

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Identifier(cryptoOperations: CryptoOperations                                                                                                                                                                                                                                                                 ,
                 resolver: IResolver?,
                 registrar: IRegistrar?) {

    /**
     * The identifier document for the identifier.
     */
    private var document: IdentifierDocument? = null


    /**
     * Initialize Identifier with Identifier Document.
     */
    constructor(document: IdentifierDocument,
                cryptoOperations: CryptoOperations,
                resolver: IResolver?,
                registrar: IRegistrar?): this(cryptoOperations, resolver, registrar) {
        this.document = document
    }

    /**
     * Initialize Identifier with Identifier string.
     */
    constructor(identifier: String,
                cryptoOperations: CryptoOperations,
                resolver: IResolver?,
                registrar: IRegistrar?): this(cryptoOperations, resolver, registrar){
        this.document = this.getDocument(identifier)
    }

    /**
     * Fetch Identifier Document from the Discovery Service.
     */
    fun getDocument(identifier: String): IdentifierDocument {
        TODO()
    }
}