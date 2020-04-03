package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.registrars.Registrar
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.Serializable

@Serializable
data class IdentifierToken (
    val document: IdentifierDocument,
    val alias: String,
    val signatureKeyReference: String,
    val encryptionKeyReference: String,
    val recoveryKeyReference: String
) {
    companion object {
        private fun tokenize(identifier: Identifier): IdentifierToken {
            return IdentifierToken(
                identifier.document,
                identifier.alias,
                identifier.signatureKeyReference,
                identifier.encryptionKeyReference,
                identifier.recoveryKeyReference
            )
        }

        fun serialize(identifier: Identifier): String {
            val token =
                tokenize(identifier)
            return Serializer.stringify(IdentifierToken.serializer(), token)
        }

        fun deserialize(
            identifierToken: String,
            cryptoOperations: CryptoOperations,
            resolver: Resolver,
            registrar: Registrar
        ): Identifier {
            val token = Serializer.parse(IdentifierToken.serializer(), identifierToken)
            return Identifier(
                alias = token.alias,
                document = token.document,
                signatureKeyReference = token.signatureKeyReference,
                encryptionKeyReference = token.encryptionKeyReference,
                recoveryKeyReference = token.recoveryKeyReference,
                cryptoOperations = cryptoOperations,
                resolver = resolver,
                registrar = registrar
            )
        }
    }
}