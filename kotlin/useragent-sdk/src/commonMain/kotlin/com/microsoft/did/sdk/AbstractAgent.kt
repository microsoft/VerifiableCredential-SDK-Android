/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.auth.oidc.OidcRequest
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.identifier.Identifier
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
                              /* private */ val cryptoOperations: CryptoOperations) {
    companion object {
        const val defaultResolverUrl = "https://beta.discover.did.microsoft.com/1.0/identifiers"
        const val defaultRegistrationUrl = "https://beta.ion.microsoft.com/api/1.0/register"
        const val defaultSignatureKeyReference = "signature"
        const val defaultEncryptionKeyReference = "encryption"
    }

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
                          redirectUrl: String,
                          nonce: String?,
                          state: String?): OidcRequest {
        TODO("Not implemented")
    }

    /**
     * Verify the signature and
     * return OIDC Request object.
     */
    fun parseOidcRequest(request: String): OidcRequest {
        TODO("Not implemented")
    }

    /**
     * Create an OIDC Response.
     */
    fun createOidcResponse(signer: Identifier,
                           request: OidcRequest
    ): OidcResponse {
        TODO("Not implemented")
    }

    /**
     * Verify the signature and
     * parse the OIDC Response object.
     */
    fun parseOidcResponse(response: String): OidcResponse {
        TODO("Not implemented")
    }

}