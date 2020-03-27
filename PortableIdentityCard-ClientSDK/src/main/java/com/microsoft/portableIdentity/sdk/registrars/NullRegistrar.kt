package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDoc
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.utilities.ILogger

class NullRegistrar(logger: ILogger): IRegistrar(logger) {
    override suspend fun register(document: RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocument {
        throw logger.error("Attempted to register from the null registrar.")
    }

    override suspend fun register(document: RegDoc, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDoc {
        throw logger.error("Attempted to register from the null registrar.")
    }
}