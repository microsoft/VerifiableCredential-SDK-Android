package com.microsoft.portableIdentity.sdk.identifier.models.payload.document

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class to represent document payload required to make a Create request on Sidetree
 * @class
 */
@Serializable
data class IdentifierDocumentPayload(
/*    @SerialName("@context")
    val context: String = "https://www.w3.org/2019/did/v1",
    val created: String? = null,
    val updated: String? = null,*/
    @SerialName("publicKeys")
    val publicKeys: List<IdentifierDocumentPublicKeyInput>,
    @SerialName("serviceEndpoints")
    val serviceEndpoints: List<IdentifierDocumentService> = emptyList()
)