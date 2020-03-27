package com.microsoft.portableIdentity.sdk.identifier.deprecated.document.service

import kotlinx.serialization.*

@Serializable
@SerialName("HostServiceEndpoint")
class ServiceHubEndpoint(val location: List<String>,
                         @Required @SerialName("@type")
                         val type: String = "HostServiceEndpoint"): Endpoint("schema.identity.foundation/hub") {
}