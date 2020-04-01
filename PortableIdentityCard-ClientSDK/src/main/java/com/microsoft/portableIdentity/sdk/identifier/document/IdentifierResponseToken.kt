package com.microsoft.portableIdentity.sdk.identifier.document

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.IdentifierResponse
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.Serializable

@Serializable
data class IdentifierResponseToken (
    val document: IdentifierDocument,
    val alias: String,
    val signatureKeyReference: String,
    val encryptionKeyReference: String
) {
    companion object {
        private fun tokenize(identifier: IdentifierResponse): IdentifierResponseToken {
            return IdentifierResponseToken(
                identifier.document,
                identifier.alias,
                identifier.signatureKeyReference,
                identifier.encryptionKeyReference
            )
        }

        fun serialize(identifier: IdentifierResponse): String {
            val token = tokenize(identifier)
            return Serializer.stringify(IdentifierResponseToken.serializer(), token)
        }

        fun deserialize(
            identifierToken: String,
            cryptoOperations: CryptoOperations,
            logger: ILogger,
            resolver: IResolver,
            registrar: IRegistrar
        ): IdentifierResponse {
            val token = Serializer.parse(IdentifierResponseToken.serializer(), identifierToken)
            return IdentifierResponse(
                document = token.document,
                alias = token.alias,
                signatureKeyReference = token.signatureKeyReference,
                encryptionKeyReference = token.encryptionKeyReference,
                cryptoOperations = cryptoOperations
            )
        }
    }
}