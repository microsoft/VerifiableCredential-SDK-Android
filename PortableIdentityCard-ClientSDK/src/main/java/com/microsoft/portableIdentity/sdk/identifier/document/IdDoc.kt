package com.microsoft.portableIdentity.sdk.identifier.document

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdDoc(
    @SerialName("@context")
    val context: String = "https://www.w3.org/2019/did/v1",
    val created: String? = null,
    val updated: String? = null,
    val publicKey: List<IdentifierDocPublicKey>,
    val recoveryKey: RecoveryKey
) {
    val id: String = ""
}