/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.datasource.repository.IdentifierRepository
import com.microsoft.did.sdk.identifier.IdentifierCreator
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.MAIN_IDENTIFIER_REFERENCE
import com.microsoft.did.sdk.util.controlflow.RepositoryException
import com.microsoft.did.sdk.util.controlflow.Result
import com.microsoft.did.sdk.util.controlflow.runResultTry
import com.microsoft.did.sdk.util.log.SdkLog
import com.nimbusds.jose.jwk.OctetSequenceKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides methods to create, update and manage decentralized identifiers.
 */
@Singleton
class IdentifierManager @Inject constructor(
    private val identifierRepository: IdentifierRepository,
    private val identifierCreator: IdentifierCreator,
    private val keyStore: EncryptedKeyStore
) {

    suspend fun getMasterIdentifier(): Result<Identifier> {
        val identifier = identifierRepository.queryByName(MAIN_IDENTIFIER_REFERENCE)
        return if (identifier != null) {
            Result.Success(identifier)
        } else {
            createMasterIdentifier()
        }
    }

    private suspend fun createMasterIdentifier(): Result<Identifier> {
        return runResultTry {
            val seed = CryptoOperations.generateSeed()
            keyStore.storeKey(MAIN_IDENTIFIER_REFERENCE, OctetSequenceKey.Builder(seed).build())
            val identifier = identifierCreator.create(MAIN_IDENTIFIER_REFERENCE)
            SdkLog.v("Created Identifier: $identifier")
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

    suspend fun getOrCreatePairwiseIdentifier(identifier: Identifier, peerId: String): Result<Identifier> {
        return runResultTry {
            val pairwiseName = identifierCreator.pairwiseIdentifierName(identifier.id, peerId)
            val pairwiseIdentifier = identifierRepository.queryByName(pairwiseName) ?: createPairwiseIdentifier(identifier, peerId)
            Result.Success(pairwiseIdentifier)
        }
    }

    private suspend fun createPairwiseIdentifier(identifier: Identifier, peerId: String): Identifier {
        val registeredIdentifier = identifierCreator.createPairwiseId(identifier, peerId)
        identifierRepository.insert(registeredIdentifier)
        return registeredIdentifier
    }
}