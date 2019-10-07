package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.registrars.IRegistrar

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Identifier private constructor (private val cryptoOperations: CryptoOperations,
                 val document: IdentifierDocument,
                 private val signatureKeyReference: String,
                 private val encryptionKeyReference: String,
                 private val resolver: IResolver,
                 private val registrar: IRegistrar) {

    /**
     * Initialize Identifier with Identifier Document.
     */
    constructor(document: IdentifierDocument,
                signatureKeyReference: String,
                encryptionKeyReference: String,
                cryptoOperations: CryptoOperations,
                resolver: IResolver,
                registrar: IRegistrar): this(
        cryptoOperations,
        document,
        signatureKeyReference,
        encryptionKeyReference,
        resolver,
        registrar)

    /**
     * Initialize Identifier with Identifier string.
     */
    constructor(identifier: String,
                cryptoOperations: CryptoOperations,
                resolver: IResolver,
                registrar: IRegistrar): this(
        cryptoOperations,
        resolver.resolve(identifier),
        "",
        (resolver.resolve(identifier).publicKeys.filter{
            if (it.publicKeyJwk.use != null) {
                it.publicKeyJwk.use == KeyUse.Encryption.value
            } else if (it.publicKeyJwk.key_ops != null) {
                it.publicKeyJwk.key_ops!!.contains(KeyUsage.Encrypt.value)
            } else {
                false
            }
        }).firstOrNull()?.publicKeyJwk?.kid ?: "",
        resolver,
        registrar)
}