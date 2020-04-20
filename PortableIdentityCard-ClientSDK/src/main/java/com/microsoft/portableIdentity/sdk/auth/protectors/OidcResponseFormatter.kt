/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.oidc.AttestationResponse
import com.microsoft.portableIdentity.sdk.utilities.Constants
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.responses.IssuanceResponse
import com.microsoft.portableIdentity.sdk.auth.responses.OidcResponse
import com.microsoft.portableIdentity.sdk.auth.responses.PresentationResponse
import com.microsoft.portableIdentity.sdk.auth.responses.Response
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiablePresentationContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiablePresentationDescriptor
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import com.microsoft.portableIdentity.sdk.utilities.controlflow.CryptoException
import com.microsoft.portableIdentity.sdk.utilities.controlflow.Result
import com.microsoft.portableIdentity.sdk.utilities.controlflow.TokenFormatterException
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

/**
 * Class that forms Response Contents Properly.
 */
@Singleton
class OidcResponseFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val signer: TokenSigner
) : Formatter {

    override fun formAndSignResponse(response: Response, responder: Identifier, expiresIn: Int): Result<String> {
        if (response !is OidcResponse) {
            val exception = TokenFormatterException("Response type does not match OidcResponse")
            return Result.Failure(exception)
        }

        return try {
            val contents = formContents(response, responder, expiresIn)
            val signedToken = signContents(contents, responder)
            Result.Success(signedToken)
        } catch (exception: Exception) {
            Result.Failure(CryptoException("Unable to sign response contents", exception))
        }
    }

    private fun signContents(contents: OidcResponseContent, responder: Identifier): String {
        val serializedResponseContent = Serializer.stringify(OidcResponseContent.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun formContents(response: OidcResponse, responder: Identifier, expiresIn: Int = Constants.RESPONSE_EXPIRATION_IN_MINUTES): OidcResponseContent {
        val (iat, exp) = createIatAndExp(expiresIn)
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.document.id

        var contract: String? = null
        var nonce: String? = null
        var state: String? = null

        when (response) {
            is IssuanceResponse -> {
                contract = response.contractUrl
            }
            is PresentationResponse -> {
                nonce = response.nonce
                state = response.state
            }
        }

        val attestationResponse = createAttestationResponse(response, responder, iat, exp)

        return OidcResponseContent(
            sub = key.getThumbprint(cryptoOperations, Sha.Sha256),
            aud = response.audience,
            nonce = nonce,
            did = did,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            state = state,
            jti = jti,
            contract = contract,
            attestations = attestationResponse
        )
    }

    private fun createAttestationResponse(response: OidcResponse, responder: Identifier, iat: Long, exp: Long): AttestationResponse? {
        return when (response) {
            is IssuanceResponse -> createIssuanceAttestationResponse(response, responder, iat, exp)
            is PresentationResponse -> createPresentationAttestationResponse(response, responder, iat, exp)
            else -> throw TokenFormatterException("Response Type not Supported.")
        }
    }

    private fun createIssuanceAttestationResponse(response: IssuanceResponse, responder: Identifier, iat: Long, exp: Long): AttestationResponse {
        var selfIssuedAttestations: Map<String, String>? = null
        var tokenAttestations: Map<String, String>? = null
        if (!response.getIdTokenBindings().isNullOrEmpty()) {
            tokenAttestations = response.getIdTokenBindings()
        }
        if (!response.getSelfIssuedClaimBindings().isNullOrEmpty()) {
            selfIssuedAttestations = response.getSelfIssuedClaimBindings()
        }
        val presentationAttestation = createPresentations(response.getCardBindings(), response, responder, iat, exp)
        return AttestationResponse(selfIssuedAttestations, tokenAttestations, presentationAttestation)
    }

    private fun createPresentationAttestationResponse(response: PresentationResponse, responder: Identifier, iat: Long, exp: Long): AttestationResponse {
        val presentationAttestation = createPresentations(response.getCardBindings(), response, responder, iat, exp)
        return AttestationResponse(null, null, presentationAttestation)
    }

    private fun createPresentations(typeToCardsMapping: Map<String, PortableIdentityCard>, response: OidcResponse, responder: Identifier, iat: Long, exp: Long): Map<String, String>? {
        val presentations = mutableMapOf<String, String>()
        typeToCardsMapping.forEach {
            presentations[it.key] = createPresentation(it.value, response, responder, iat, exp)
        }
        if (presentations.isEmpty()) {
            return null
        }
        return presentations
    }

    // only support one VC per VP
    private fun createPresentation(card: PortableIdentityCard, response: OidcResponse, responder: Identifier, iat: Long, exp: Long): String {
        val vp = VerifiablePresentationDescriptor(verifiableCredential = listOf(card.verifiableCredential.raw))
        val jti = UUID.randomUUID().toString()
        val did = responder.document.id
        val contents = VerifiablePresentationContent(
            jti = jti,
            vp = vp,
            sub = response.audience,
            iss = did,
            iat = iat,
            nbf = iat,
            exp = exp
        )
        val serializedContents = Serializer.stringify(VerifiablePresentationContent.serializer(), contents)
        return signer.signWithIdentifier(serializedContents, responder)
    }

    private fun createIatAndExp(expiresIn: Int = Constants.RESPONSE_EXPIRATION_IN_MINUTES): Pair<Long, Long> {
        val currentTime = Date().time
        val expiresInMilliseconds = 1000 * 60 * expiresIn
        val expiration = currentTime + expiresInMilliseconds.toLong()
        val exp = floor(expiration / 1000f).toLong()
        val iat = floor(currentTime / 1000f).toLong()
        return Pair(iat, exp)
    }
}