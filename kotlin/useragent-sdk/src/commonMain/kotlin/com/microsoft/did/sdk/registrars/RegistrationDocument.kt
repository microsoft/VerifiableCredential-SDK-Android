package com.microsoft.did.sdk.registrars

import com.microsoft.did.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.identifier.IdentifierDocumentService
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationDocument(val id: String? = null, val publicKeys: ArrayList<IdentifierDocumentPublicKey>, val services: List<IdentifierDocumentService>)