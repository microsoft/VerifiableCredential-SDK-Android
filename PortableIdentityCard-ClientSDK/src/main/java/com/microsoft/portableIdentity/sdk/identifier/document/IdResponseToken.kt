package com.microsoft.portableIdentity.sdk.identifier.document

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Id
import com.microsoft.portableIdentity.sdk.identifier.IdResponse
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.Serializable

@Serializable
data class IdResponseToken (
    val document: IdDoc
) {
    companion object {
        private fun tokenize(identifier: IdResponse): IdResponseToken {
            return IdResponseToken(
                identifier.document
            )
        }

        fun serialize(identifier: IdResponse): String {
            val token = tokenize(identifier)
            return Serializer.stringify(IdResponseToken.serializer(), token)
        }

        fun deserialize(
            identifierToken: String,
            cryptoOperations: CryptoOperations,
            logger: ILogger,
            resolver: IResolver,
            registrar: IRegistrar
        ): IdResponse {
            val token = Serializer.parse(IdResponseToken.serializer(), identifierToken)
            return IdResponse(
                document = token.document
            )
        }
    }
}