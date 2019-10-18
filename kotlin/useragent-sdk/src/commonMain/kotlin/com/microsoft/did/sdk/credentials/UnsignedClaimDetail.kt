package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.resolvers.IResolver
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("unsigned")
data class UnsignedClaimDetail(
    val data: List<Map<String, String>>
): ClaimDetail {
    @Required
    override val type: String
        get() = "unsigned"

    override suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver) {
        // nothing to do
    }
}