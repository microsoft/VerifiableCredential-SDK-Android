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
import io.ktor.http.ContentType
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
     * Verify the signature and
     * return OIDC Request object.
     */
    @ImplicitReflectionSerializer
    suspend fun parseOidcRequest(request: String): OidcRequest {
        return OidcRequest.parseAndVerify(request, cryptoOperations, resolver)
    }

    /**
     * Verify the signature and
     * parse the OIDC Response object.
     */
    @ImplicitReflectionSerializer
    suspend fun parseOidcResponse(response: String,
                                  clockSkewInMinutes: Int = 5,
                                  issuedWithinLastMinutes: Int? = null,
                                  contentType: ContentType = ContentType.Application.FormUrlEncoded): OidcResponse {
        return OidcResponse.parseAndVerify(response, clockSkewInMinutes, issuedWithinLastMinutes,
            cryptoOperations, resolver, contentType)
    }

}