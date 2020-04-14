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
        val did = if (identifierRepository.queryByName(IDENTIFIER_SECRET_KEY_NAME) != null) {
            SdkLog.d("Identifier found, de-serializing")
            identifierRepository.queryByName(IDENTIFIER_SECRET_KEY_NAME)
        } else {
            SdkLog.d("No identifier found, registering new DID")
            val identifier = registerPortableIdentity()
            identifier
        }
        SdkLog.d("Using identifier ${did.id}")
        return did
    }

    private fun registerPortableIdentity(): Identifier {
        var did: Identifier? = null
        // TODO: Verify runBlocking is proper here
        runBlocking {
            did = createPortableIdentity()
        }
        SdkLog.d("Created ${did!!.id}")
        return did!!
    }

    fun createPortableIdentity(callback: (Identifier) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createPortableIdentity())
        }
    }

    /**
     * Creates an Identifier.
     */
    private suspend fun createPortableIdentity(): Identifier {
        return withContext(Dispatchers.Default) {
            createAndRegisterPortableIdentity()
        }
    }

    private suspend fun createAndRegisterPortableIdentity(): Identifier {
        val identifier =  registrar.register(SIGNATURE_KEYREFERENCE, RECOVERY_KEYREFERENCE, cryptoOperations)
        identifierRepository.insert(identifier)
        return identifier
    }
}