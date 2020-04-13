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
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiablePresentationContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiablePresentationDescriptor
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.math.floor

/**
 * Class that forms Response Contents Properly.
 */
@Singleton
class OidcResponseFormatter @Inject constructor(
        private val cryptoOperations: CryptoOperations,
        @Named("signatureKeyReference") private val signatureKeyReference: String,
        private val signer: OidcResponseSigner
) {

    fun formContents(response: OidcResponse, responder: Identifier, useKey: String = signatureKeyReference, expiresIn: Int = Constants.RESPONSE_EXPIRATION_IN_MINUTES): OidcResponseContent {
        val (iat, exp) = createIatAndExp(1000)
        val key = cryptoOperations.keyStore.getPublicKey(useKey).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id

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
        var selfIssuedAttestations: String? = null
        var tokenAttestations: Map<String, String>? = null
        if (response is IssuanceResponse) {
            if (!response.getIdTokenBindings().isNullOrEmpty()) {
                tokenAttestations = response.getIdTokenBindings()
            }
            if (!response.getSelfIssuedClaimBindings().isNullOrEmpty()) {
                val serializedSelfIssued = Serializer.stringify(response.getSelfIssuedClaimBindings(), String::class, String::class)
                val token = JwsToken(serializedSelfIssued)
                selfIssuedAttestations = token.serialize()
            }
        }
        val presentationAttestation = createPresentations(response.getCardBindings(), response, responder, iat, exp)
        return AttestationResponse(selfIssuedAttestations, tokenAttestations, presentationAttestation)
    }

    // TODO(wrap VC in a VP and map it to the type)
    private fun createPresentations(typeToCardsMapping: Map<String, PortableIdentityCard>,
                                    response: OidcResponse,
                                    responder: Identifier,
                                    iat: Long,
                                    exp: Long): Map<String, String>? {
        var presentations = mutableMapOf<String, String>()
        typeToCardsMapping.forEach {
            presentations[it.key] = createPresentation(it.value, response, responder, iat, exp)
        }

        if (presentations.isEmpty()) {
            return null
        }

        return presentations
    }

    // only support one VC per VP
    private fun createPresentation(card: PortableIdentityCard,
                                   response: OidcResponse,
                                   responder: Identifier,
                                   iat: Long,
                                   exp: Long): String {
        val vp = VerifiablePresentationDescriptor(verifiableCredential = listOf(card.verifiableCredential.raw))
        val jti = UUID.randomUUID().toString()
        val did = responder.id
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
        val token = signer.sign(serializedContents, responder)
        return token.serialize()

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