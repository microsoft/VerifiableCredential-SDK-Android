/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.datasource.repository

import com.microsoft.did.sdk.datasource.db.SdkDatabase
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.datasource.network.apis.ApiProvider
import com.microsoft.did.sdk.datasource.network.identifierOperations.ResolveIdentifierNetworkOperation
import javax.inject.Inject

class IdentifierRepository @Inject constructor(database: SdkDatabase, private val apiProvider: ApiProvider) {
    private val identifierDao = database.identifierDao()

    suspend fun resolveIdentifier(url: String, identifier: String) = ResolveIdentifierNetworkOperation(
        apiProvider, url, identifier
    ).fire()

    fun insert(identifier: Identifier) = identifierDao.insert(identifier)

    fun queryByIdentifier(identifier: String): Identifier = identifierDao.queryByIdentifier(identifier)

    fun queryByName(name: String): Identifier? = identifierDao.queryByName(name)
}