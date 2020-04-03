package com.microsoft.portableIdentity.sdk.credentials.deprecated

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.resolvers.Resolver

interface ClaimDetail {
    val type: String

    suspend fun verify(cryptoOperations: CryptoOperations, resolver: Resolver)
}