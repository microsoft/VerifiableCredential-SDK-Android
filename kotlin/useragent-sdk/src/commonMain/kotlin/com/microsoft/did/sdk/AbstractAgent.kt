/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.document.IdentifierDocumentPublicKey
import com.microsoft.did.sdk.identifier.document.service.IdentityHubUserService
import com.microsoft.did.sdk.identifier.document.service.UserHubEndpoint
import com.microsoft.did.sdk.registrars.RegistrationDocument
import com.microsoft.did.sdk.registrars.SidetreeRegistrar
import com.microsoft.did.sdk.resolvers.HttpResolver
import kotlinx.serialization.ImplicitReflectionSerializer


/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
abstract class AbstractAgent (registrationUrl: String,
                              resolverUrl: String,
                              val signatureKeyReference: String,
                              val encryptionKeyReference: String,
                              keyStore: IKeyStore,
                              subtleCrypto: SubtleCrypto) {
    companion object {
        const val defaultRegistrationUrl = "beta.discover.did.microsoft.com/1.0/identifiers/"
        const val defaultResolverUrl = "beta.ion.microsoft.com/api/1.0/register"
        const val defaultSignatureKeyReference = "signature"
        const val defaultEncryptionKeyReference = "encryption"
    }

    /**
     * CryptoOperations
     */
    private val cryptoOperations =  CryptoOperations(subtleCrypto = subtleCrypto, keyStore = keyStore)
    /**
     * Registrar to be used when registering Identifiers.
     */
    private val registrar = SidetreeRegistrar(registrationUrl)

    /**
     * Resolver to be used when resolving Identifier Documents.
     */
    private val resolver = HttpResolver(resolverUrl)

    /**
     * Creates and registers an Identifier.
     */
    @ImplicitReflectionSerializer
    suspend fun createIdentifier(): Identifier {
        return Identifier.createAndRegister("a", cryptoOperations, signatureKeyReference,
            encryptionKeyReference, resolver, registrar, listOf("did:test:hub.id"))
    }

    /**
     * Creates an OIDC Request.
     */
    fun createOidcRequest(signer: Identifier,
                          signingKeyReference: String?,
                          redirectUrl: String,
                          nonce: String?,
                          state: String?) {
        TODO("Not implemented")
    }

    /**
     * Verify the signature and
     * return OIDC Request object.
     */
    fun parseOidcRequest(request: String) {
        TODO("Not implemented")
    }

    /**
     * Create an OIDC Response.
     */
    fun createOidcResponse(signer: Identifier,
                           signingKeyReference: String?,
                           request: OidcRequest) {
        TODO("Not implemented")
    }

    /**
     * Verify the signature and
     * parse the OIDC Response object.
     */
    fun parseOidcResponse(response: String) {
        TODO("Not implemented")
    }

}