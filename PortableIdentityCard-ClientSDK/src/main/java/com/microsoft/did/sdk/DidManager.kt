/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.DidSdkConfig.cryptoOperations
import com.microsoft.did.sdk.DidSdkConfig.encryptionKeyReference
import com.microsoft.did.sdk.DidSdkConfig.logger
import com.microsoft.did.sdk.DidSdkConfig.registrar
import com.microsoft.did.sdk.DidSdkConfig.resolver
import com.microsoft.did.sdk.DidSdkConfig.signatureKeyReference
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierToken
import com.microsoft.did.sdk.utilities.Base64Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.security.auth.callback.Callback
import kotlin.random.Random

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
object DidManager {

    @JvmStatic
    fun createIdentifier(callback: (Identifier) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createIdentifier())
        }
    }

    /**
     * Creates and registers an Identifier.
     */
    suspend fun createIdentifier(): Identifier {
        return withContext(Dispatchers.Default) {
            val alias = Base64Url.encode(Random.nextBytes(16), logger = logger)
            Identifier.createAndRegister(
                alias, cryptoOperations, logger, signatureKeyReference,
                encryptionKeyReference, resolver, registrar, listOf("did:test:hub.id")
            )
        }
    }

    fun deserializeIdentifier(identifierToken: String): Identifier {
        return IdentifierToken.deserialize(
            identifierToken,
            cryptoOperations,
            logger,
            resolver,
            registrar
        )
    }
}