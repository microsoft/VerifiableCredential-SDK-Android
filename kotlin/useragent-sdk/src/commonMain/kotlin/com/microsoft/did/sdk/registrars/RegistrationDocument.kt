package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.identifier.IdentifierDocumentService
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