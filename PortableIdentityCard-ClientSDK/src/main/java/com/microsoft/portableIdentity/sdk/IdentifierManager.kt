/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import androidx.lifecycle.LiveData
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.IdentifierCreator
import com.microsoft.portableIdentity.sdk.repository.IdentifierRepository
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.portableIdentity.sdk.utilities.Constants.IDENTIFIER_SECRET_KEY_NAME
import com.microsoft.portableIdentity.sdk.utilities.Constants.METHOD_NAME
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.RepositoryException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Success
import com.microsoft.portableIdentity.sdk.utilities.controlflow.runResultTry
import com.microsoft.portableIdentity.sdk.utilities.stringToByteArray
import kotlinx.coroutines.*
import java.security.MessageDigest
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

    suspend fun getMasterIdentifier(): Result<Identifier> {
        return withContext(Dispatchers.IO) { getOrCreateMasterIdentifier() }
    }

    private suspend fun getOrCreateMasterIdentifier(): Result<Identifier> {
        val identifier = identifierRepository.queryByName(IDENTIFIER_SECRET_KEY_NAME)
        return if (identifier != null) {
            Result.Success(identifier)
        } else {
            createMasterIdentifier()
        }
    }

    // Master Identifier will be created once per app.
    private suspend fun createMasterIdentifier(): Result<Identifier> {
        return runResultTry {
            //TODO(seed is needed for pairwise key generation)
            cryptoOperations.generateAndStoreSeed()
            // peer id for master Identifier will be method name for now.
            val identifier = identifierCreator.create(METHOD_NAME).abortOnError()
            SdkLog.i("Creating Identifier: $identifier")
            saveIdentifier(identifier)
        }
    }

    suspend fun createPairwiseIdentifier(identifier: Identifier, peerId: String): Result<Identifier> {
        return runResultTry {
            when (val pairwiseIdentifier = identifierRepository.queryByName(pairwiseIdentifierName(peerId))) {
                null -> {
                    val registeredIdentifier = identifierCreator.createPairwiseId(identifier.id, peerId).abortOnError()
                    saveIdentifier(registeredIdentifier)
                }
                else -> Result.Success(pairwiseIdentifier)
            }

        }
    }

    private fun pairwiseIdentifierName(peerId: String): String {
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64Url.encode(digest.digest(stringToByteArray(peerId)))
    }

    private fun saveIdentifier(identifier: Identifier): Result<Identifier> {
        return try {
            identifierRepository.insert(identifier)
            Result.Success(identifier)
        } catch (exception: Exception) {
            throw RepositoryException("Unable to save identifier in repository", exception)
        }
    }

    fun getIdentifierByName(name: String): Identifier? {
        return identifierRepository.queryByName(name)
    }
}