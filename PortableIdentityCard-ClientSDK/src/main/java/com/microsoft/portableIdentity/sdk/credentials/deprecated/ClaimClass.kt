package com.microsoft.portableIdentity.sdk.credentials.deprecated

import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.getHttpClient
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
            val description: String? = null
        )
    }

    companion object {
        suspend fun resolve(url: String): ClaimClass {
            val document = getHttpClient().get<String>(url)
            return deserialize(document)
        }

        fun deserialize(claimClass: String): ClaimClass {
            return Serializer.parse(ClaimClass.serializer(), claimClass)
        }
    }

    fun serialize(): String {
        return Serializer.stringify(ClaimClass.serializer(), this)
    }
}