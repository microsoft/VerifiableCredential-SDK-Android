package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.identifier.document.IdentifierDocument

class NullRegistrar: IRegistrar() {
    override suspend fun register(document: RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocument {
        throw Error("Attempted to register from the null registrar.")
    }
}