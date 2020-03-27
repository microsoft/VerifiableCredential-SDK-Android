package com.microsoft.portableIdentity.sdk.registrars

import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocPublicKey
import com.microsoft.portableIdentity.sdk.identifier.document.service.IdentifierDocService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("document")
data class RegistrationDoc(
    @SerialName("@context")
    val context: String = "https://www.w3.org/2019/did/v1",
    val id: String? = null,
    @SerialName("publicKey")
    val publicKeys: List<IdentifierDocPublicKey>,
    @SerialName("service")
    val services: List<IdentifierDocService>? = null
)