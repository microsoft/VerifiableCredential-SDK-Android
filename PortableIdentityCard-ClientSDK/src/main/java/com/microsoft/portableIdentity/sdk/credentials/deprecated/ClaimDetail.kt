package com.microsoft.portableIdentity.sdk.credentials.deprecated

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

interface ClaimDetail {
    val type: String

    suspend fun verify(cryptoOperations: CryptoOperations, resolver: IResolver)
}