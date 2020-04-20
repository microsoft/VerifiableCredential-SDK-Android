/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.SecretKey
import com.microsoft.portableIdentity.sdk.crypto.keys.ellipticCurve.EllipticCurvePrivateKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.identifier.IdentifierToken
import com.microsoft.portableIdentity.sdk.identifier.document.IdentifierDocument
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.Base64Url
import com.microsoft.portableIdentity.sdk.utilities.Serializer
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
    private val resolver: IResolver,
    private val registrar: IRegistrar,
    private val serializer: Serializer
) {

    private val didSecretName = "did.identifier"

    private val signatureKeyReference = "signature"
    private val encryptionKeyReference = "encryption"

    val did: Identifier by lazy { initDid() }

    // TODO: Cleanup method
    private fun initDid(): Identifier {
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
    }

    // TODO: properly name APIs
    fun registerNewDid(): Identifier {
        var did: Identifier? = null
        // TODO: Verify runBlocking is proper here
        runBlocking {
            did = createIdentifier()
        }
        println("Registered ${did!!.document.id}")
        return did!!
    }

    private suspend fun createTempIdentifier(): Identifier {
        val did = "did:ion:test:EiCAvQuaAu5awq_e_hXyJImdQ5-xJsZzzQ3Xd9a2EAphtQ"
        val document = resolver.resolveDocument(did)
        val jsonWebKey = JsonWebKey( kid = "#sigKey",
        x= "AU+WZrK8O/rx4wlq3idyuFlvACM/sMXZputpkzyHPMk=",
        y= "qOpL6upm2RSrwrTBbUvL/4xYnSTdSFLtjOlQlJ74pt0=",
        d= "JB4hmMeNb0cownkvkoY83HZRZBowqz7DhsFSlpeq0JY=")
        val subtle =
            cryptoOperations.subtleCryptoFactory.getMessageSigner(W3cCryptoApiConstants.EcDsa.value, SubtleCryptoScope.Private)
        cryptoOperations.keyStore.save(
            "sigKey",
            EllipticCurvePrivateKey(jsonWebKey)
        )
        return Identifier(
            alias = "alias",
            document = document,
            signatureKeyReference = "sigKey",
            encryptionKeyReference = "encKey",
            cryptoOperations = cryptoOperations,
            resolver = resolver,
            registrar = registrar,
            serializer = serializer
        )
    }

    fun createIdentifier(callback: (Identifier) -> Unit) {
        GlobalScope.launch {
            callback.invoke(createTempIdentifier())
        }
    }

    /**
     * Creates and registers an Identifier.
     */
    suspend fun createIdentifier(): Identifier {
        return withContext(Dispatchers.Default) {
            val alias = Base64Url.encode(Random.nextBytes(16))
            Identifier.createAndRegister(
                alias, cryptoOperations, signatureKeyReference,
                encryptionKeyReference, resolver, registrar, serializer, listOf("did:test:hub.id")
            )
        }
    }

    fun deserializeIdentifier(identifierToken: String): Identifier {
        return IdentifierToken.deserialize(
            identifierToken,
            cryptoOperations,
            resolver,
            registrar,
            serializer
        )
    }
}