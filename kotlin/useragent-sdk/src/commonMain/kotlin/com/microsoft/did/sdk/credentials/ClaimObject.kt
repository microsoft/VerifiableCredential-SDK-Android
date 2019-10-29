package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.ILogger
import com.microsoft.did.sdk.utilities.MinimalJson
import kotlinx.serialization.ImplicitReflectionSerializer
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
            return MinimalJson.serializer.parse(ClaimObject.serializer(), claimObject)
        }
    }

    fun serialize(): String {
        return MinimalJson.serializer.stringify(ClaimObject.serializer(), this)
    }

    suspend fun getClaimClass(): ClaimClass {
        return ClaimClass.resolve(claimClass)
    }

    @ImplicitReflectionSerializer
    suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver, logger: ILogger) {
        claimDetails.verify(cryptoOperations, resolver, logger = logger)
    }
}