package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

class NullRegistrar() : Registrar() {

    override suspend fun register(
        signatureKeyReference: String,
        encryptionKeyReference: String,
        recoveryKeyReference: String,
        cryptoOperations: CryptoOperations
    ): Identifier {
        throw SdkLog.error("Attempted to register from the null registrar.")
    }
}