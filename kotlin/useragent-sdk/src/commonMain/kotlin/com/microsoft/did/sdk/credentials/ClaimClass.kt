package com.microsoft.did.sdk.credentials

import kotlinx.serialization.Serializable

@Serializable
data class ClaimClass(
    val issuerName: String? = null,
    val claimLogo: ClaimLogo? = null,
    val claimName: String? = null,
    val hexBackgroundColor: String? = null,
    val hexFontColor: String? = null,
    val moreInfo: String? = null,
    val helpLinks: Map<String, String>? = null,
    val claimDescriptions: List<ClaimDescription>? = null,
    val readPermissionDescription: PermissionDescription? = null
) {
    @Serializable
    data class ClaimLogo(
        val sourceUri: SourceUri
    ) {
        @Serializable
        data class SourceUri(
            val uri: String,
            val description: String
        )
    }
}