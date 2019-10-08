package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ServiceHubEndpoint(val locations: List<String>,
                         @SerialName("@type")
                         val type: String? = "HostServiceEndpoint"): Endpoint()