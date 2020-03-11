package com.microsoft.portableIdentity.sdk.credentials

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.ILogger

interface ClaimDetail {
    val type: String

    suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver, logger: ILogger)
}