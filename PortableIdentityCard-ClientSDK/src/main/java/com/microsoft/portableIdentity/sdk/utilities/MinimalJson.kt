package com.microsoft.portableIdentity.sdk.utilities

import com.microsoft.portableIdentity.sdk.credentials.ClaimDetail
import com.microsoft.portableIdentity.sdk.credentials.SignedClaimDetail
import com.microsoft.portableIdentity.sdk.credentials.UnsignedClaimDetail
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.IdentifierDocumentService
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.Endpoint
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.IdentityHubService
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.ServiceHubEndpoint
import com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service.UserHubEndpoint
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

    private val claimDetailSerializer = SerializersModule {
        polymorphic(ClaimDetail::class) {
            UnsignedClaimDetail::class with UnsignedClaimDetail.serializer()
            SignedClaimDetail::class with SignedClaimDetail.serializer()
        }
    }

    val serializer = Json(
        context = identifierDocumentServiceSerializer + serviceEndpointSerializer + claimDetailSerializer,
        configuration = JsonConfiguration(
            encodeDefaults = false,
            strictMode = false
    ))

}