package com.microsoft.portableIdentity.sdk.identifier.models.document

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.models.document.service.IdentityHubService
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdentifierDocument(
    val publicKey: List<IdentifierDocumentPublicKey>,
    val service: List<IdentityHubService>,
    val recoveryKey: JsonWebKey
) {
    var id: String = ""
}