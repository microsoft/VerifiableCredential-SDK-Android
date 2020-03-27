package com.microsoft.portableIdentity.sdk.identifier.document

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class IdentifierDocPatch (
    val action: String,
/*    @SerialName("publicKeys")
    val publicKeys: List<IdentifierDocPublicKey>*/
    @SerialName("document")
    val document: IdentifierDoc
){}