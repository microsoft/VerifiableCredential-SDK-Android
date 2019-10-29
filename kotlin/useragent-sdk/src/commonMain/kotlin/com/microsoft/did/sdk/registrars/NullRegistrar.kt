package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.identifier.document.IdentifierDocument
import com.microsoft.did.sdk.utilities.ILogger

class NullRegistrar(logger: ILogger): IRegistrar(logger) {
    override suspend fun register(document: RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocument {
        throw logger.error("Attempted to register from the null registrar.")
    }
}