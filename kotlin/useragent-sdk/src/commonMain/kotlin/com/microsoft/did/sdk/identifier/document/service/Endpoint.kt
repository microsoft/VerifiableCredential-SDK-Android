package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class Endpoint(
    @Required @SerialName("@context")
    open val context: String = "schema.identity.foundation/hub")