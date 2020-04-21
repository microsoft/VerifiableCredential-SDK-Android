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
import com.microsoft.portableIdentity.sdk.utilities.SdkLog
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

    val did: Identifier by lazy { initLongFormDid() }

    private fun initLongFormDid(): Identifier {
        val did = identifierRepository.queryByName(IDENTIFIER_SECRET_KEY_NAME)
        return if (did != null) {
            SdkLog.d("Identifier found, de-serializing")
            did
        } else {
            SdkLog.d("No identifier found, registering new DID")
            val identifier = registerPortableIdentity()
            identifier
        }
    }

    private fun registerPortableIdentity(): Identifier {
        // TODO: Verify runBlocking is proper here
        var did: Identifier? = null
        runBlocking {
            val id = createPortableIdentity()
            if (id is Result.Success)
                did = id.payload
        }
        return did!!
    }

    fun createPortableIdentity(callback: (Result<Identifier>) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createPortableIdentity())
        }
    }

    /**
     * Creates an Identifier.
     */
    private suspend fun createPortableIdentity(): Result<Identifier> {
        return withContext(Dispatchers.Default) {
            createAndRegisterPortableIdentity()
        }
    }

    private suspend fun createAndRegisterPortableIdentity(): Result<Identifier> {
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