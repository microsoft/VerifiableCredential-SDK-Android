package com.microsoft.portableIdentity.sdk.credentials

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimObject(val claimClass: String,
                       @SerialName("@context")
                       val context: String,
                       @SerialName("@type")
                       val type: String,
                       val claimDescriptions: List<ClaimDescription>,
                       val claimIssuer: String,
                       val claimDetails: ClaimDetail) {
    companion object {
        fun deserialize(claimObject: String): ClaimObject {
            return Serializer.parse(ClaimObject.serializer(), claimObject)
        }
    }

    fun serialize(): String {
        return Serializer.stringify(ClaimObject.serializer(), this)
    }

    suspend fun getClaimClass(): ClaimClass {
        return ClaimClass.resolve(claimClass)
    }

    suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver, logger: ILogger) {
        claimDetails.verify(cryptoOperations, resolver, logger = logger)
    }
}