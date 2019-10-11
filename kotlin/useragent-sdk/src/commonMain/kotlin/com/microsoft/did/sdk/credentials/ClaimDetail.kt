package com.microsoft.did.sdk.credentials

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.resolvers.IResolver

interface ClaimDetail {
    val type: String

    suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver)
}