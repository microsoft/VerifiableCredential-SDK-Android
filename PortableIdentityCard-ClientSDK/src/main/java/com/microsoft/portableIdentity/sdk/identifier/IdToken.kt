package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDoc
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.Serializable

@Serializable
data class IdToken (
    val document: IdentifierDoc,
    val alias: String,
    val signatureKeyReference: String,
    val encryptionKeyReference: String
) {
    companion object {
        private fun tokenize(identifier: Id): IdToken {
            return IdToken(
                identifier.document,
                identifier.alias,
                identifier.signatureKeyReference,
                identifier.encryptionKeyReference
            )
        }

        fun serialize(identifier: Id): String {
            val token = tokenize(identifier)
            return Serializer.stringify(IdToken.serializer(), token)
        }

        fun deserialize(
            identifierToken: String,
            cryptoOperations: CryptoOperations,
            logger: ILogger,
            resolver: IResolver,
            registrar: IRegistrar
        ): Id {
            val token = Serializer.parse(IdToken.serializer(), identifierToken)
            return Id(
                alias = token.alias,
                document = token.document,
                signatureKeyReference = token.signatureKeyReference,
                encryptionKeyReference = token.encryptionKeyReference,
                cryptoOperations = cryptoOperations,
                logger = logger,
                resolver = resolver,
                registrar = registrar
            )
        }
    }
}