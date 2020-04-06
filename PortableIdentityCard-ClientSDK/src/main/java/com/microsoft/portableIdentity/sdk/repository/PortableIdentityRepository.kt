package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.identifier.LongformIdentifier
import com.microsoft.portableIdentity.sdk.identifier.models.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.PortableIdentityNetworkOperation
import javax.inject.Inject

class PortableIdentityRepository @Inject constructor(
    database: SdkDatabase,
    private val identityNetworkOperation: PortableIdentityNetworkOperation
) {
    val portableIdentityDao = database.portableIdentityDao()

    suspend fun resolveIdentifier(url: String, identifier: String): IdentifierDocument {
        val identifierDocument = identityNetworkOperation.resolveIdentifier(url, identifier)
        identifierDocument!!.id = identifier
        return identifierDocument
    }

    fun insert(longformIdentifier: LongformIdentifier) = portableIdentityDao.insert(longformIdentifier)

    fun query(identifier: String): LongformIdentifier = portableIdentityDao.queryIdentifier(identifier)
}