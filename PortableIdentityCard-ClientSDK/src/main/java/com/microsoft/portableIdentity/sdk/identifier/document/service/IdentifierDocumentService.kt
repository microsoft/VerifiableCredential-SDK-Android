package com.microsoft.portableIdentity.sdk.identifier

import com.microsoft.portableIdentity.sdk.identifier.document.service.Endpoint
import kotlinx.serialization.Polymorphic

interface IdentifierDocumentService {
    val id: String
    val type: String
    @Polymorphic
    val serviceEndpoint: Endpoint
}
