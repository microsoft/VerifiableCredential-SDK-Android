package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocumentPayload
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.registrars.deprecated.RegistrationDocument
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

class NullRegistrar(): IRegistrar() {
    override suspend fun register(document: RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocument {
        throw SdkLog.error("Attempted to register from the null registrar.")
    }

    override suspend fun register(document: com.microsoft.portableIdentity.sdk.registrars.RegistrationDocument, signatureKeyRef: String, crypto: CryptoOperations): IdentifierDocumentPayload {
        throw SdkLog.error("Attempted to register from the null registrar.")
    }
}