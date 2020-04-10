package com.microsoft.portableIdentity.sdk.identifier.models.document.service

interface IdentifierDocumentService {
    val id: String
    val type: String
    val serviceEndpoint: String
}