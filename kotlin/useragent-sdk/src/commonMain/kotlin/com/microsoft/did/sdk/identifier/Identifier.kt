package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.IResolver
import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.registrars.IRegistrar
import com.microsoft.did.sdk.registrars.SidetreeRegistrar

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param keyStore to store keys associated with Identifier.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Identifier(keyStore: IKeyStore?,
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
                keyStore: IKeyStore?,
                resolver: IResolver?,
                registrar: IRegistrar?): this(keyStore, resolver, registrar) {
        this.document = document
    }

    /**
     * Initialize Identifier with Identifier string.
     */
    constructor(identifier: String,
                keyStore: IKeyStore?,
                resolver: IResolver?,
                registrar: IRegistrar?): this(keyStore, resolver, registrar){
        this.document = this.getDocument(identifier)
    }

    /**
     * Fetch Identifier Document from the Discovery Service.
     */
    fun getDocument(identifier: String): IdentifierDocument {
        TODO()
    }
}