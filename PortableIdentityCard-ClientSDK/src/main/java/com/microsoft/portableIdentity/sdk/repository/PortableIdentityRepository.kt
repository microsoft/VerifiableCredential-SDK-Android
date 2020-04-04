package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.identifier.LongformIdentifier
import com.microsoft.portableIdentity.sdk.repository.networking.PortableIdentityNetworkOperation
import javax.inject.Inject

class PortableIdentityRepository @Inject constructor(
    database: SdkDatabase,
    private val identityNetworkOperation: PortableIdentityNetworkOperation
) {
    val portableIdentityDao = database.portableIdentityDao()

    suspend fun resolveIdentifier(url: String/*, identifier: String*/) = identityNetworkOperation.resolveIdentifier(url/*, identifier*/)

    fun insert(longformIdentifier: LongformIdentifier) = portableIdentityDao.insert(longformIdentifier)

    fun query(identifier: String): LongformIdentifier = portableIdentityDao.queryIdentifier(identifier)
}