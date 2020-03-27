package com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service

import kotlinx.serialization.Polymorphic

interface IdentifierDocumentService {
    val id: String
    val type: String
    @Polymorphic
    val serviceEndpoint: Endpoint
}
