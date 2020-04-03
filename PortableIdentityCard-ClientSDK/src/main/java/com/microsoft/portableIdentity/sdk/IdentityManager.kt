/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.SecretKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
//import com.microsoft.portableIdentity.sdk.identifier.response.IdentifierResponse
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.IdentifierToken
//import com.microsoft.portableIdentity.sdk.identifier.response.IdentifierResponseToken
import com.microsoft.portableIdentity.sdk.registrars.Registrar
import com.microsoft.portableIdentity.sdk.resolvers.Resolver
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
@Singleton
class IdentityManager @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val resolver: Resolver,
    private val registrar: Registrar,
    @Named("signatureKeyReference") private val signatureKeyReference: String,
    @Named("encryptionKeyReference") private val encryptionKeyReference: String,
    @Named("recoveryKeyReference") private val recoveryKeyReference: String
) {

    private val didSecretName = "did.identifier"

//    val did: IdentifierResponse by lazy { initLongFormDid() }
    val did = initLongFormDid()

    // TODO: Cleanup method
/*  private fun initDid(): Identifier {
        val did = if (cryptoOperations.keyStore.list().containsKey(didSecretName)) {
            println("Identifier found, deserializing")
            val keySerialized = cryptoOperations.keyStore.getSecretKey(didSecretName).getKey()
            deserializeIdentifier(keySerialized.k!!)
        } else {
            println("No identifier found, registering new DID")
            val identifier = registerNewDid()
            val key = SecretKey(
                JsonWebKey(
                    kty = KeyType.Octets.value,
                    kid = "#$didSecretName.1",
                    k = identifier.serialize()
                )
            )
            cryptoOperations.keyStore.save(didSecretName, key)
            identifier
        }
        println("Using identifier ${did.document.id}")
        return did
    }*/

    private fun initLongFormDid(): Identifier {
        val did = if (cryptoOperations.keyStore.list().containsKey(didSecretName)) {
            println("Identifier found, deserializing")
            val keySerialized = cryptoOperations.keyStore.getSecretKey(didSecretName).getKey()
            deserializeIdentifier(keySerialized.k!!)
        } else {
            println("No identifier found, registering new DID")
            val identifier = registerNewLongFormDid()
            val key = SecretKey(
                JsonWebKey(
                    kty = KeyType.Octets.value,
                    kid = "#$didSecretName.1",
                    //TODO: Save only signing key or recovery key as well?
                    k = identifier.serialize()
                )
            )
            cryptoOperations.keyStore.save(didSecretName, key)
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

    private fun registerNewLongFormDid(): Identifier {
        var did: Identifier? = null
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

    fun createLongFormIdentifier(callback: (Identifier) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createLongFormIdentifier())
        }
    }

    /**
     * Creates and registers an Identifier.
     */
/*    private suspend fun createIdentifier(): com.microsoft.portableIdentity.sdk.identifier.deprecated.Identifier {
        return withContext(Dispatchers.Default) {
            val alias = Base64Url.encode(Random.nextBytes(16))
            Identifier.createAndRegister(
                alias, cryptoOperations, signatureKeyReference,
                encryptionKeyReference, resolver, registrar, listOf("did:test:hub.id")
            )
        }
    }*/

    suspend fun createLongFormIdentifier(): Identifier {
        return withContext(Dispatchers.Default) {
            val alias = Base64Url.encode(Random.nextBytes(16))
            Identifier.createLongFormIdentifier(
                alias, cryptoOperations, signatureKeyReference,
                encryptionKeyReference, recoveryKeyReference, resolver, registrar
            )
        }
    }

    private fun deserializeIdentifier(identifierToken: String): Identifier {
        return IdentifierToken.deserialize(
            identifierToken,
            cryptoOperations,
            resolver,
            registrar
        )
    }
}