package com.microsoft.did.sdk.identifier.models.payload.document

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class to represent document payload required to make a Create request on Sidetree
 * @class
 */
@Serializable
data class IdentifierDocumentPayload(
    @SerialName("publicKeys")
    val publicKeys: List<IdentifierDocumentPublicKeyInput>,
    @SerialName("service_endpoints")
    var serviceEndpoints: List<IdentifierDocumentService> = emptyList()
)