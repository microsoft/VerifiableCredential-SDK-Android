/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.registrars.Registrar
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
import com.microsoft.portableIdentity.sdk.utilities.Constants.IDENTIFIER_SECRET_KEY_NAME
import com.microsoft.portableIdentity.sdk.utilities.Constants.RECOVERY_KEYREFERENCE
import com.microsoft.portableIdentity.sdk.utilities.Constants.SIGNATURE_KEYREFERENCE
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
    private val registrar: Registrar
) {

    suspend fun getIdentifier(): Result<Identifier> {
        return withContext(Dispatchers.IO) { initLongFormIdentifier() }
    }

    private suspend fun initLongFormIdentifier(): Result<Identifier> {
        return when (val identifier = identifierRepository.queryByName(IDENTIFIER_SECRET_KEY_NAME)) {
            null -> createAndRegisterNewIdentifier()
            else -> Result.Success(identifier)
        }
    }

    private suspend fun createAndRegisterNewIdentifier(): Result<Identifier> {
        return runResultTry {
            val registeredIdentifier = registrar.register(SIGNATURE_KEYREFERENCE, RECOVERY_KEYREFERENCE, cryptoOperations).abortOnError()
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