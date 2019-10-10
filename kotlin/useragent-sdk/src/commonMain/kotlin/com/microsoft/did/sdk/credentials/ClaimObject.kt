package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.utilities.MinimalJson
import kotlinx.serialization.Serializable

@Serializable
data class ClaimObject(val claimClass: String,
                       val claimDescriptions: List<ClaimDescription>,
                       val claimIssuer: String,
                       val claimDetails: List<ClaimDetail>) {
    companion object {
        fun deserialize(claimObject: String): ClaimObject {
            return MinimalJson.serializer.parse(ClaimObject.serializer(), claimObject)
        }
    }

    fun serialize(): String {
        return MinimalJson.serializer.stringify(ClaimObject.serializer(), this)
    }
}