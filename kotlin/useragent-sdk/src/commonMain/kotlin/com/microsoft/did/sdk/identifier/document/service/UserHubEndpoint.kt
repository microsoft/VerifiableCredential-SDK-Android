package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserHubEndpoint(
    val instances: List<String>,
    @Required @SerialName("@type")
    val type: String = "UserServiceEndpoint"): Endpoint() {
    @Required
    override val context: String = "https://schema.identity.foundation/hub"
}