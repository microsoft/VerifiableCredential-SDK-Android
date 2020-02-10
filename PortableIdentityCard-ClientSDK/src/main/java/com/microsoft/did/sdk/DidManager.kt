/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk

import android.content.Context
import com.microsoft.did.sdk.auth.oidc.OidcRequest
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.identifier.Identifier
import com.microsoft.did.sdk.identifier.IdentifierToken
import com.microsoft.did.sdk.registrars.SidetreeRegistrar
import com.microsoft.did.sdk.resolvers.HttpResolver
import com.microsoft.did.sdk.utilities.Base64Url
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.ILogger
import io.ktor.http.ContentType
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.random.Random

/**
 * Class for creating identifiers and
 * sending and parsing OIDC Requests and Responses.
 * @class
 */
class DidManager(
    context: Context,
    registrationUrl: String = defaultRegistrationUrl,
    resolverUrl: String = defaultResolverUrl,
    private val signatureKeyReference: String = defaultSignatureKeyReference,
    private val encryptionKeyReference: String = defaultEncryptionKeyReference,
    private val cryptoOperations: CryptoOperations,
    private val logger: ILogger = ConsoleLogger()
) {

    companion object {
        const val defaultResolverUrl = "https://beta.discover.did.microsoft.com/1.0/identifiers"
        const val defaultRegistrationUrl = "https://beta.ion.microsoft.com/api/1.0/register"
        const val defaultSignatureKeyReference = "signature"
        const val defaultEncryptionKeyReference = "encryption"
    }

    init {
        val keyStore = AndroidKeyStore(context, logger)
        val subtleCrypto = AndroidSubtle(keyStore, logger)
        val crypto = CryptoOperations(subtleCrypto, keyStore, logger)
        val ecSubtle = EllipticCurveSubtleCrypto(subtleCrypto, logger)
        crypto.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
    }

    /**
     * Registrar to be used when registering Identifiers.
     */
    private val registrar = SidetreeRegistrar(registrationUrl, logger)

    /**
     * Resolver to be used when resolving Identifier Documents.
     */
    private val resolver = HttpResolver(resolverUrl, logger)

    /**
     * Creates and registers an Identifier.
     */
    @ImplicitReflectionSerializer
    suspend fun createIdentifier(): Identifier {
        val alias = Base64Url.encode(Random.nextBytes(16), logger = logger)
        return Identifier.createAndRegister(
            alias, cryptoOperations, logger, signatureKeyReference,
            encryptionKeyReference, resolver, registrar, listOf("did:test:hub.id")
        )
    }

    fun deserializeIdentifier(identifierToken: String): Identifier {
        return IdentifierToken.deserialize(identifierToken, cryptoOperations, logger, resolver, registrar)
    }

    /**
     * Verify the signature and
     * return OIDC Request object.
     */
    @ImplicitReflectionSerializer
    suspend fun parseOidcRequest(request: String): OidcRequest {
        return OidcRequest.parseAndVerify(request, cryptoOperations, logger, resolver)
    }

    /**
     * Verify the signature and
     * parse the OIDC Response object.
     */
    @ImplicitReflectionSerializer
    suspend fun parseOidcResponse(
        response: String,
        clockSkewInMinutes: Int = 5,
        issuedWithinLastMinutes: Int? = null,
        contentType: ContentType = ContentType.Application.FormUrlEncoded
    ): OidcResponse {
        return OidcResponse.parseAndVerify(
            response, clockSkewInMinutes, issuedWithinLastMinutes,
            cryptoOperations, logger, resolver, contentType
        )
    }
}