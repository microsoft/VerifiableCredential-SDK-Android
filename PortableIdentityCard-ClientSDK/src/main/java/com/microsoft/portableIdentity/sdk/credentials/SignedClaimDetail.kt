package com.microsoft.portableIdentity.sdk.credentials

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.DidKeyResolver
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("jws")
data class SignedClaimDetail(
    val data: String
): ClaimDetail {
    @Required
    override val type: String
        get() = "jws"

    override suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver, logger: ILogger) {
        val claimDetail = JwsToken(data, logger = logger)
        DidKeyResolver.verifyJws(claimDetail, cryptoOperations, resolver, logger = logger)
    }
}