package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
//import com.microsoft.portableIdentity.sdk.identifier.response.IdentifierResponse
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.registrars.RegistrationDocument
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.byteArrayToString
import java.security.MessageDigest

/**
 * Class for creating and managing identifiers,
 * retrieving identifier documents.
 * @class
 * @param cryptoOperations Crypto Operations.
 * @param resolver to resolve the Identifier Document for Identifier.
 * @param registrar to register Identifiers.
 */
class Identifier constructor(
    val document: IdentifierDocument,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val recoveryKeyReference: String,
    val alias: String,
    private val cryptoOperations: CryptoOperations,
    private val resolver: IResolver,
    private val registrar: IRegistrar
) {
    companion object {
        // TODO: needs refactoring! Dependency inject this object instead of having this companion etc.
        suspend fun createLongFormIdentifier(
            alias: String,
            cryptoOperations: CryptoOperations,
            signatureKeyReference: String,
            encryptionKeyReference: String,
            recoveryKeyReference: String,
            resolver: IResolver,
            registrar: IRegistrar
        ): Identifier {
            // TODO: Use software generated keys from the seed
//        val seed = cryptoOperations.generateSeed()
//        val publicKey = cryptoOperations.generatePairwise(seed)
            val personaEncKeyRef = "$alias.$encryptionKeyReference"
            val personaSigKeyRef = "$alias.$signatureKeyReference"
            val personaRecKeyRef = "$alias.$recoveryKeyReference"
            val payloadGenerator = PayloadGenerator(
                cryptoOperations,
                signatureKeyReference,
                encryptionKeyReference,
                recoveryKeyReference
            )
            val registrationDocumentEncoded = payloadGenerator.generateCreatePayload(alias)
            val registrationDocument = Serializer.parse(RegistrationDocument.serializer(), byteArrayToString(Base64Url.decode(registrationDocumentEncoded)))

            val uniqueSuffix = payloadGenerator.computeUniqueSuffix(registrationDocument.suffixData)
            val portableIdentity = "did:ion:test:$uniqueSuffix"

            val identifierDocument = resolver.resolve(portableIdentity, registrationDocumentEncoded, cryptoOperations)
            SdkLog.debug("Registered new decentralized identity")
            return Identifier(
                alias = alias,
                document = identifierDocument.document,
                signatureKeyReference = personaSigKeyRef,
                encryptionKeyReference = personaEncKeyRef,
                recoveryKeyReference = personaRecKeyRef,
                cryptoOperations = cryptoOperations,
                resolver = resolver,
                registrar = registrar
            )
        }
    }

    fun serialize(): String {
        return IdentifierToken.serialize(this)
    }

}
