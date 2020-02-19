package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.ILogger
import com.microsoft.did.sdk.utilities.IPolymorphicSerialization
import com.microsoft.did.sdk.utilities.PolymorphicSerialization
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
            val polymorphicSerialization: IPolymorphicSerialization = PolymorphicSerialization
            return polymorphicSerialization.parse(ClaimObject.serializer(), claimObject)
//            return MinimalJson.serializer.parse(ClaimObject.serializer(), claimObject)
        }
    }

    fun serialize(): String {
        val polymorphicSerialization: IPolymorphicSerialization = PolymorphicSerialization
        return polymorphicSerialization.stringify(ClaimObject.serializer(), this)
//        return MinimalJson.serializer.stringify(ClaimObject.serializer(), this)
    }

    suspend fun getClaimClass(): ClaimClass {
        return ClaimClass.resolve(claimClass)
    }

    suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver, logger: ILogger) {
        claimDetails.verify(cryptoOperations, resolver, logger = logger)
    }
}