package com.microsoft.portableIdentity.sdk.repository

import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.PortableIdentityNetworkOperation
import javax.inject.Inject

class PortableIdentityRepository @Inject constructor(
    database: SdkDatabase,
    private val identityNetworkOperation: PortableIdentityNetworkOperation
) {
    private val portableIdentityDao = database.portableIdentityDao()

    suspend fun resolveIdentifier(url: String, identifier: String): IdentifierDocument {
        return identityNetworkOperation.resolveIdentifier(url, identifier)!!
    }

    fun insert(identifier: Identifier) = portableIdentityDao.insert(identifier)

    fun queryById(identifier: String): Identifier = portableIdentityDao.queryById(identifier)

    fun queryByName(name: String): Identifier = portableIdentityDao.queryByName(name)
}