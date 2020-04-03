package com.microsoft.portableIdentity.sdk.identifier.models.document.service

//TODO: Check if there can be any type of endpoints and remove polymorphic serialization if possible
interface IdentifierDocumentService {
    val id: String
    val type: String
    val serviceEndpoint: String
}