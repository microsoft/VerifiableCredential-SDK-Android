/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.registrars.SidetreeRegistrar
import com.microsoft.did.sdk.resolvers.HttpResolver

const val defaultRegistrationUrl = "beta.discover.did.microsoft.com"
const val defaultResolverUrl = "beta.ion.microsoft.com"

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
class Agent (registrationUrl: String = defaultRegistrationUrl,
             resolverUrl: String = defaultResolverUrl) {

    /**
     * CryptoOperations
     */
    private val cryptoOperations =  CryptoOperations()
    /**
     * Registrar to be used when registering Identifiers.
     */
    private val registrar = SidetreeRegistrar(registrationUrl, cryptoOperations)

    /**
     * Resolver to be used when resolving Identifier Documents.
     */
    private val resolver = HttpResolver(resolverUrl)

    /**
     * Creates and registers an Identifier.
     */
    fun createIdentifier() {

    }

    /**
     * Creates an OIDC Request.
     */
    fun createOidcRequest(signer: Identifier,
                          signingKeyReference: String?,
                          redirectUrl: String,
                          nonce: String?,
                          state: String?) {}

    /**
     * Verify the signature and
     * return OIDC Request object.
     */
    fun parseOidcRequest(request: String) {
        throw Error("Not implemented")
    }

    /**
     * Create an OIDC Response.
     */
    fun createOidcResponse(signer: Identifier,
                           signingKeyReference: String?,
                           request: OidcRequest) {
        throw Error("Not implemented")
    }

    /**
     * Verify the signature and
     * parse the OIDC Response object.
     */
    fun parseOidcResponse(response: String) {
        throw Error("Not implemented")
    }

}