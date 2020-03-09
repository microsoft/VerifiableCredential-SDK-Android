package com.microsoft.portableIdentity.sdk.identifier.document

import com.microsoft.portableIdentity.sdk.identifier.IdentifierDocumentService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class to represent Identifier Documents.
 * @class
 */
@Serializable
class IdentifierDocument(val id: String,
                         @SerialName("publicKey")
                         val publicKeys: List<IdentifierDocumentPublicKey>,
                         @SerialName("service")
                         val services: List<IdentifierDocumentService>,
                         @SerialName("@context")
                         val context: String = "https://www.w3.org/2019/portableIdentity/v1",
                         val created: String? = null,
                         val updated: String? = null
                         ) {
}