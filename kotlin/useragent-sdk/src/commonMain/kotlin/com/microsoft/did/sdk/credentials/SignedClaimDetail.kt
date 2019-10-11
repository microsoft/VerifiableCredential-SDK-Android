package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.resolvers.IResolver
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("JWS")
data class SignedClaimDetail(
    val data: String
): ClaimDetail {
    @Required
    override val type: String
        get() = "JWS"

    @ImplicitReflectionSerializer
    override suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver) {
        val claimDetail = JwsToken(data)
        DidKeyResolver.verifyJws(claimDetail, cryptoOperations, resolver)
    }
}