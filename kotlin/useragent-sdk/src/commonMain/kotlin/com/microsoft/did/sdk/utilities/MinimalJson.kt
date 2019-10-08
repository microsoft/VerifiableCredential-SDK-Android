package com.microsoft.did.sdk.utilities

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

object MinimalJson {
    val serializer = Json(
        JsonConfiguration(
    encodeDefaults = false,
    strictMode = false
    )
    )
}