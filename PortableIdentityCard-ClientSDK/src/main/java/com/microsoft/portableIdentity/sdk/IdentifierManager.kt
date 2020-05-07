/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.IdentifierCreator
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
import com.microsoft.portableIdentity.sdk.utilities.Constants.IDENTIFIER_SECRET_KEY_NAME
import com.microsoft.portableIdentity.sdk.utilities.Constants.METHOD_NAME
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.RepositoryException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.runResultTry
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
@Singleton
class IdentifierManager @Inject constructor(
    private val identifierRepository: IdentifierRepository,
    private val cryptoOperations: CryptoOperations,
    private val identifierCreator: IdentifierCreator
) {

    suspend fun getIdentifier(): Result<Identifier> {
        return withContext(Dispatchers.IO) { initLongFormIdentifier() }
    }

    private suspend fun initLongFormIdentifier(): Result<Identifier> {
        return when (val identifier = identifierRepository.queryByName(IDENTIFIER_SECRET_KEY_NAME)) {
            null -> createMasterIdentifier()
            else -> Result.Success(identifier)
        }
    }

    // Master Identifier will be created once per app.
    private suspend fun createMasterIdentifier(): Result<Identifier> {
        return runResultTry {
            //TODO(seed is needed for pairwise key generation)
            cryptoOperations.generateAndStoreSeed()
            // peer id for master Identifier will be method name for now.
            val registeredIdentifier = identifierCreator.create(METHOD_NAME).abortOnError()
            saveIdentifier(registeredIdentifier)
        }
    }

    // TODO(create pairwise Identifier based off new key generation algorithm).
    suspend fun createPairwiseIdentifier(identifier: Identifier, peerId: String): Result<Identifier> {
        return runResultTry {
            val registeredIdentifier = identifierCreator.createPairwiseId(identifier.id, peerId).abortOnError()
            saveIdentifier(registeredIdentifier)
        }
    }

    private fun saveIdentifier(identifier: Identifier): Result<Identifier> {
        return try {
            identifierRepository.insert(identifier)
            Result.Success(identifier)
        } catch (exception: Exception) {
            throw RepositoryException("Unable to save identifier in repository", exception)
        }
    }
}