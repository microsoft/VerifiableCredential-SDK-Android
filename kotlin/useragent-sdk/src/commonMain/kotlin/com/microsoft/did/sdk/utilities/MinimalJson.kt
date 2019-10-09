package com.microsoft.did.sdk.utilities

import com.microsoft.did.sdk.identifier.IdentifierDocumentService
import com.microsoft.did.sdk.identifier.document.service.Endpoint
import com.microsoft.did.sdk.identifier.document.service.IdentityHubService
import com.microsoft.did.sdk.identifier.document.service.ServiceHubEndpoint
import com.microsoft.did.sdk.identifier.document.service.UserHubEndpoint
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus

object MinimalJson {
    private val identifierDocumentServiceSerializer = SerializersModule {
        polymorphic(IdentifierDocumentService::class) {
            IdentityHubService::class with IdentityHubService.serializer()
        }
    }

    private val serviceEndpointSerializer = SerializersModule {
        polymorphic(Endpoint::class) {
            ServiceHubEndpoint::class with ServiceHubEndpoint.serializer()
            UserHubEndpoint::class with UserHubEndpoint.serializer()
        }
    }

    val serializer = Json(
        context = identifierDocumentServiceSerializer + serviceEndpointSerializer,
        configuration = JsonConfiguration(
            encodeDefaults = false,
            strictMode = false
    ))

}