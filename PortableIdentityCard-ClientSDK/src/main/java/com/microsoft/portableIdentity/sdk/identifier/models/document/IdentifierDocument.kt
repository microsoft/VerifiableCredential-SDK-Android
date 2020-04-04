package com.microsoft.portableIdentity.sdk.identifier.models.document

import com.microsoft.portableIdentity.sdk.identifier.models.document.service.IdentityHubService
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdentifierDocument(
//    val context: String = "https://www.w3.org/2019/did/v1",
    val created: String? = null,
    val updated: String? = null,
    val publicKey: List<IdentifierDocumentPublicKey>,
    val service: List<IdentityHubService>,
    val recoveryKey: RecoveryKey
) {
    val id: String = ""
}