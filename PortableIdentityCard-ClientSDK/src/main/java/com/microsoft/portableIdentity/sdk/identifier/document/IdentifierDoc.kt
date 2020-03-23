package com.microsoft.portableIdentity.sdk.identifier.document

import com.microsoft.portableIdentity.sdk.identifier.IdentifierDocService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdentifierDoc(
/*    @SerialName("recoveryKey")
    val recoveryKey: RecoveryKey,*/
    @SerialName("@context")
    val context: String = "https://www.w3.org/2019/did/v1",
    val created: String? = null,
    val updated: String? = null,
    @SerialName("publicKey")
    val publicKeys: List<IdentifierDocPublicKey>,
    @SerialName("service")
    val services: List<IdentifierDocService>

) {
    val id: String = ""
}