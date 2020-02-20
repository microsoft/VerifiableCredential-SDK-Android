/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.SecretKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierToken
import com.microsoft.did.sdk.utilities.Base64Url
import kotlinx.coroutines.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.random.Random

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
class DidManager(private val config: DidSdkConfig) {

    private val didSecretName = "did.identifier"

    @ImplicitReflectionSerializer
    val did: Identifier by lazy { initDid() }

    // TODO: Cleanup method
    @ImplicitReflectionSerializer
    private fun initDid(): Identifier {
        val did = if (config.cryptoOperations.keyStore.list().containsKey(didSecretName)) {
            println("Identifier found, deserializing")
            val keySerialized = config.cryptoOperations.keyStore.getSecretKey(didSecretName).getKey()
            deserializeIdentifier(keySerialized.k!!)
        } else {
            println("No identifier found, registering new DID")
            val identifier = registerNewDid()
            val key = SecretKey(
                JsonWebKey(
                    kty = KeyType.Octets.value,
                    kid = "#$didSecretName.1",
                    k = identifier.serialize()
                ),
                logger = config.logger
            )
            config.cryptoOperations.keyStore.save(didSecretName, key)
            identifier
        }
        println("Using identifier ${did.document.id}")
        return did
    }

    // TODO: properly name APIs
    @ImplicitReflectionSerializer
    fun registerNewDid(): Identifier {
        var did: Identifier? = null
        // TODO: Verify runBlocking is proper here
        runBlocking {
            did = createIdentifier()
        }
        println("Registered ${did!!.document.id}")
        return did!!
    }

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
            val alias = Base64Url.encode(Random.nextBytes(16), logger = config.logger)
            Identifier.createAndRegister(
                alias, config.cryptoOperations, config.logger, config.signatureKeyReference,
                config.encryptionKeyReference, config.resolver, config.registrar, listOf("did:test:hub.id")
            )
        }
    }

    fun deserializeIdentifier(identifierToken: String): Identifier {
        return IdentifierToken.deserialize(
            identifierToken,
            config.cryptoOperations,
            config.logger,
            config.resolver,
            config.registrar
        )
    }

}