package com.microsoft.portableIdentity.sdk.identifier.document.service

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class Endpoints(
    @Required @SerialName("@context")
    val context: String)