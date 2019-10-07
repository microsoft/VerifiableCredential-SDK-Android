package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.identifier.document.service.Endpoint
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable @Polymorphic
abstract class IdentifierDocumentService (val id: String, val type: String, @ContextualSerialization val serviceEndpoint: Endpoint) {
}
