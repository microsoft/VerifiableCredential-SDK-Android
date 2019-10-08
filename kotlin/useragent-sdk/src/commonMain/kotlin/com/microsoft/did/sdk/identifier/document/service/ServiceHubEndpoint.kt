package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.*

@Serializable
@SerialName("HostServiceEndpoint")
class ServiceHubEndpoint(val locations: List<String>,
                         @Required @SerialName("@type")
                         val type: String = "HostServiceEndpoint"): Endpoint() {
    @Required
    override val context: String = "https://schema.identity.foundation/hub"
}