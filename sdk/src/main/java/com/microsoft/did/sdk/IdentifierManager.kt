/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.util.Base64
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.HASHING_ALGORITHM_FOR_ID
import com.microsoft.did.sdk.util.Constants.MASTER_IDENTIFIER_NAME
import com.microsoft.did.sdk.util.controlflow.RepositoryException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.log.SdkLog
import com.microsoft.did.sdk.util.stringToByteArray
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides methods to create, update and manage decentralized identifiers.
 */
@Singleton
class IdentifierManager @Inject constructor(
    private val identifierRepository: IdentifierRepository,
    private val cryptoOperations: CryptoOperations,
    private val identifierCreator: IdentifierCreator
) {

    suspend fun getMasterIdentifier(): Result<Identifier> {
        val identifier = identifierRepository.queryByName(MASTER_IDENTIFIER_NAME)
        return if (identifier != null) {
            Result.Success(identifier)
        } else {
            createMasterIdentifier()
        }
    }

    private suspend fun createMasterIdentifier(): Result<Identifier> {
        return runResultTry {
            cryptoOperations.generateSeed(MASTER_IDENTIFIER_NAME)
            val identifier = identifierCreator.create(MASTER_IDENTIFIER_NAME)
            SdkLog.i("Creating Identifier: $identifier")
            identifierRepository.insert(identifier)
            Result.Success(identifier)
        }
    }

    suspend fun getIdentifierById(id: String): Result<Identifier> {
        val identifier = identifierRepository.queryByIdentifier(id)
        return if (identifier != null) {
            Result.Success(identifier)
        } else {
            Result.Failure(RepositoryException("Identifier doesn't exist in db."))
        }
    }

    suspend fun createPairwiseIdentifier(identifier: Identifier, peerId: String): Result<Identifier> {
        return runResultTry {
            when (val pairwiseIdentifier = identifierRepository.queryByName(pairwiseIdentifierName(peerId))) {
                null -> {
                    val registeredIdentifier = identifierCreator.createPairwiseId(identifier, peerId)
                    identifierRepository.insert(registeredIdentifier)
                    Result.Success(registeredIdentifier)
                }
                else -> Result.Success(pairwiseIdentifier)
            }
        }
    }

    private fun pairwiseIdentifierName(peerId: String): String {
        val digest = MessageDigest.getInstance(HASHING_ALGORITHM_FOR_ID)
        return Base64.encodeToString(digest.digest(stringToByteArray(peerId)), Base64.URL_SAFE)
    }
}