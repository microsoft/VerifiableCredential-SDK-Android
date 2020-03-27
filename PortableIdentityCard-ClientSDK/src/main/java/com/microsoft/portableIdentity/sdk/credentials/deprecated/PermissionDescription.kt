package com.microsoft.portableIdentity.sdk.credentials.deprecated

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PermissionDescription (
    val name: String,
    val description: String,
    @SerialName("icon_uri")
    val iconUri: String
) {
}