package com.microsoft.portableIdentity.sdk.identifier.models.document

import com.microsoft.portableIdentity.sdk.identifier.models.document.service.IdentifierDocumentService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdentifierDocumentPayload(
    @SerialName("@context")
    val context: String = "https://www.w3.org/2019/did/v1",
    val created: String? = null,
    val updated: String? = null,
    @SerialName("publicKeys")
    val publicKeys: List<IdentifierDocumentPublicKey>,
    @SerialName("serviceEndpoints")
    val serviceEndpoints: List<IdentifierDocumentService>

) {
    val id: String = ""
}