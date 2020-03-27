package com.microsoft.portableIdentity.sdk.registrars.deprecated

import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.IdentifierDocumentPublicKey
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.IdentifierDocumentService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationDocument(
    @SerialName("@context")
        val context: String = "https://www.w3.org/2019/did/v1",
    val id: String? = null,
    @SerialName("publicKey")
        val publicKeys: List<IdentifierDocumentPublicKey>,
    @SerialName("service")
        val services: List<IdentifierDocumentService>? = null)