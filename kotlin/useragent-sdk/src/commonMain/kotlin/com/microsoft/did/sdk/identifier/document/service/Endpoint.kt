package com.microsoft.did.sdk.identifier.document.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class Endpoint(
    @SerialName("@context")
    open val context: String? = null)