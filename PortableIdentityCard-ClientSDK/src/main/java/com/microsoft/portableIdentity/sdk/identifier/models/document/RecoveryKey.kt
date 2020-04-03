package com.microsoft.portableIdentity.sdk.identifier.models.document

import kotlinx.serialization.Serializable

@Serializable
data class RecoveryKey(
    val publicKeyHex: String
) {
}