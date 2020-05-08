/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.auth.models.oidc.AttestationResponse
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.cards.verifiableCredential.VerifiableCredential
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.models.Sha
import com.microsoft.portableIdentity.sdk.identifier.Identifier
import com.microsoft.portableIdentity.sdk.utilities.Serializer
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
    private val serializer: Serializer,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter,
    private val signer: TokenSigner
) {

    fun format(
        responder: Identifier,
        audience: String,
        requestedVcs: Map<String, VerifiableCredential>,
        requestedIdTokens: Map<String, String>,
        requestedSelfIssuedClaims: Map<String, String>,
        contract: String? = null,
        nonce: String? = null,
        state: String? = null,
        transformingVerifiableCredential: VerifiableCredential? = null,
        recipientIdentifier: String? = null,
        expiresIn: Int
    ): String {
        val (iat, exp) = createIatAndExp(expiresIn)
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val attestationResponse =
            this.createAttestationResponse(requestedVcs, requestedIdTokens, requestedSelfIssuedClaims, audience, responder, iat, exp)

        val contents = OidcResponseContent(
            sub = key.getThumbprint(cryptoOperations, Sha.Sha256),
            aud = audience,
            nonce = nonce,
            did = did,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            state = state,
            jti = jti,
            contract = contract,
            attestations = attestationResponse,
            vc = transformingVerifiableCredential?.raw,
            recipient = recipientIdentifier
        )
        return signContents(contents, responder)
    }

    private fun signContents(contents: OidcResponseContent, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContent.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationResponse(
        requestedVcs: Map<String, VerifiableCredential>,
        requestedIdTokens: Map<String, String>,
        requestedSelfIssuedClaims: Map<String, String>,
        audience: String,
        responder: Identifier,
        iat: Long,
        exp: Long
    ): AttestationResponse? {
        if (areNoCollectedClaims(requestedVcs, requestedIdTokens, requestedSelfIssuedClaims)) {
            return null
        }
        var presentationAttestations: Map<String, String>? = null
        var idTokenAttestations: Map<String, String>? = null
        var selfIssuedAttestations: Map<String, String>? = null
        if (requestedVcs.isNotEmpty()) {
            presentationAttestations = createPresentations(requestedVcs, audience, responder, iat, exp)
        }
        if (requestedIdTokens.isNotEmpty()) {
            idTokenAttestations = requestedIdTokens
        }
        if (requestedSelfIssuedClaims.isNotEmpty()) {
            selfIssuedAttestations = requestedSelfIssuedClaims
        }
        return AttestationResponse(selfIssuedAttestations, idTokenAttestations, presentationAttestations)
    }

    private fun createPresentations(
        requestedVcs: Map<String, VerifiableCredential>,
        audience: String,
        responder: Identifier,
        iat: Long,
        exp: Long
    ): Map<String, String>? {
        val presentations = requestedVcs.mapValues { verifiablePresentationFormatter.createPresentation(listOf(it.value), audience, responder, iat, exp) }
        if (presentations.isEmpty()) {
            return null
        }
        return presentations
    }

    private fun createIatAndExp(expiresIn: Int): Pair<Long, Long> {
        val currentTime = Date().time
        val expiresInMilliseconds = 1000 * 60 * expiresIn
        val expiration = currentTime + expiresInMilliseconds.toLong()
        val exp = floor(expiration / 1000f).toLong()
        val iat = floor(currentTime / 1000f).toLong()
        return Pair(iat, exp)
    }

    private fun areNoCollectedClaims(
        requestedVcs: Map<String, VerifiableCredential>,
        requestedIdTokens: Map<String, String>,
        requestedSelfIssuedClaims: Map<String, String>
    ): Boolean {
        return (requestedVcs.isEmpty() && requestedIdTokens.isEmpty() && requestedSelfIssuedClaims.isEmpty())
    }
}