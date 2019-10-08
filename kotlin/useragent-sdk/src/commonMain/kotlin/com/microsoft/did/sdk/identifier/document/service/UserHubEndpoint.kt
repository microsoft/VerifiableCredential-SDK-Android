package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserHubEndpoint(
    val instances: List<String>,
    @SerialName("@type")
    val type: String? = "UserServiceEndpoint"): Endpoint()