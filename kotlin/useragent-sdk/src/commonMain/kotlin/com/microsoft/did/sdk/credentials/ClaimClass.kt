package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.utilities.MinimalJson
import com.microsoft.did.sdk.utilities.getHttpClient
import io.ktor.client.request.get
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

    companion object {
        suspend fun resolve(url: String): ClaimClass {
            val document = getHttpClient().get<String>(url)
            return deserialize(document)
        }

        fun deserialize(claimClass: String): ClaimClass {
            return MinimalJson.serializer.parse(ClaimClass.serializer(), claimClass)
        }
    }

    fun serialize(): String {
        return MinimalJson.serializer.stringify(ClaimClass.serializer(), this)
    }
}