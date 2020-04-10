package com.microsoft.portableIdentity.sdk.identifier.models.document

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.Serializable

@Serializable
data class RecoveryKey(
    //val publicKeyHex: String
    val recoveryKey: JsonWebKey
) {
}