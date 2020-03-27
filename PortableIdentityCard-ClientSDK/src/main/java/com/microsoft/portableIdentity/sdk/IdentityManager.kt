/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.SecretKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.identifier.IdentifierResponse
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierResponseToken
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
class IdentityManager(private val config: DidSdkConfig) {

    private val didSecretName = "did.identifier"

    val did: IdentifierResponse by lazy { initLongFormDid() }

    internal val cryptoOperations: CryptoOperations = config.cryptoOperations

    internal val resolver: IResolver = config.resolver

    // TODO: Cleanup method
/*    private fun initDid(): Identifier {
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
    }*/

    private fun initLongFormDid(): IdentifierResponse {
        val did = if (config.cryptoOperations.keyStore.list().containsKey(didSecretName)) {
            println("Identifier found, deserializing")
            val keySerialized = config.cryptoOperations.keyStore.getSecretKey(didSecretName).getKey()
            deserializeIdentifier(keySerialized.k!!)
        } else {
            println("No identifier found, registering new DID")
            val identifier = registerNewLongFormDid()
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
/*    fun registerNewDid(): com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier {
        var did: com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier? = null
        // TODO: Verify runBlocking is proper here
        runBlocking {
            did = createIdentifier()
        }
        println("Registered ${did!!.document.id}")
        return did!!
    }*/

    private fun registerNewLongFormDid(): IdentifierResponse {
        var did: IdentifierResponse? = null
        // TODO: Verify runBlocking is proper here
        runBlocking {
            did = createLongFormIdentifier()
        }
        println("Registered ${did!!.document.id}")
        return did!!
    }

/*    fun createIdentifier(callback: (com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createIdentifier())
        }
    }*/

    fun createLongFormIdentifier(callback: (IdentifierResponse) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createLongFormIdentifier())
        }
    }

    /**
     * Creates and registers an Identifier.
     */
/*    private suspend fun createIdentifier(): com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier {
        return withContext(Dispatchers.Default) {
            val alias = Base64Url.encode(Random.nextBytes(16), logger = config.logger)
            com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier.createAndRegister(
                alias, config.cryptoOperations, config.logger, config.signatureKeyReference,
                config.encryptionKeyReference, config.resolver, config.registrar, listOf("did:test:hub.id")
            )
        }
    }*/

    private suspend fun createLongFormIdentifier(): IdentifierResponse {
        return withContext(Dispatchers.Default) {
            val alias = Base64Url.encode(Random.nextBytes(16), logger = config.logger)
            Identifier.createLongFormIdentifier(
                alias, config.cryptoOperations, config.logger, config.signatureKeyReference,
                config.encryptionKeyReference, config.resolver, config.registrar
            )
        }
    }

    private fun deserializeIdentifier(identifierToken: String): IdentifierResponse {
        return IdentifierResponseToken.deserialize(
            identifierToken,
            config.cryptoOperations,
            config.logger,
            config.resolver,
            config.registrar
        )
    }
}