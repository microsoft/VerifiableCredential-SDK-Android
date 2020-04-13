package com.microsoft.portableIdentity.sdk.repository

import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument.IdentifierDocument
import com.microsoft.portableIdentity.sdk.repository.networking.IdentifierNetworkOperation
import javax.inject.Inject

class IdentifierRepository @Inject constructor(
    database: SdkDatabase,
    private val identifierNetworkOperation: IdentifierNetworkOperation
) {
    private val identifierDao = database.identifierDao()

    suspend fun resolveIdentifier(url: String, identifier: String): IdentifierDocument {
        return identifierNetworkOperation.resolveIdentifier(url, identifier)!!
    }

    fun insert(identifier: Identifier) = identifierDao.insert(identifier)

    fun queryByIdentifier(identifier: String): Identifier = identifierDao.queryByIdentifier(identifier)

    fun queryByName(name: String): Identifier = identifierDao.queryByName(name)
}