package com.microsoft.portableIdentity.sdk.repository

import com.microsoft.portableIdentity.sdk.repository.networking.PortableIdentityNetworkOperation
import javax.inject.Inject

class PortableIdentityRepository @Inject constructor(
    database: SdkDatabase,
    private val identityNetworkOperation: PortableIdentityNetworkOperation
) {
    suspend fun resolveIdentifier(url: String/*, identifier: String*/) = identityNetworkOperation.resolveIdentifier(url/*, identifier*/)
}