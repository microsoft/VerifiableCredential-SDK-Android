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
    companion object {
        var microsoftIdentityHubDocument: IdentifierDocumentPayload = IdentifierDocumentPayload(
            publicKeys = listOf(
                IdentifierDocumentPublicKey(
                    id = "#key1",
                    type = "Secp256k1VerificationKey2018",
                    publicKeyHex = "02f49802fb3e09c6dd43f19aa41293d1e0dad044b68cf81cf7079499edfd0aa9f1"
                )
            )/*,
            services = listOf(
                IdentityHubService(
                    id = "IdentityHub",
                    publicKey = "#key1",
                    serviceEndpoint = "https://testendpoint"*//*UserHubEndpoint(listOf("did:bar:456", "did:zaz:789"))*//*
                )
            )*/
        )
    }

    fun serialize(): String {
        return IdentifierResponseToken.serialize(this)
    }
}
