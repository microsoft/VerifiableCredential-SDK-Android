/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.repository

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.repository.networking.apis.ApiProvider
import com.microsoft.portableIdentity.sdk.repository.networking.identifierOperations.ResolveIdentifierNetworkOperation
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