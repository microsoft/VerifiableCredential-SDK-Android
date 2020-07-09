/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.FormatterException
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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
        responseAudience: String,
        presentationsAudience: String? = null,
        expiresIn: Int,
        requestedVcs: Map<String, VerifiableCredential>? = null,
        requestedIdTokens: Map<String, String>? = null,
        requestedSelfIssuedClaims: Map<String, String>? = null,
        contract: String? = null,
        nonce: String? = null,
        state: String? = null,
        transformingVerifiableCredential: VerifiableCredential? = null,
        recipientIdentifier: String? = null,
        revocationRPs: List<String>? = null,
        revocationReason: String? = null
    ): String {
        val (iat, exp) = createIatAndExp(expiresIn)
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val attestationResponse = this.createAttestationClaimModel(
            requestedVcs,
            requestedIdTokens,
            requestedSelfIssuedClaims,
            presentationsAudience,
            responder,
            expiresIn)

        val contents = OidcResponseContent(
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm),
            aud = responseAudience,
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
            recipient = recipientIdentifier,
            rp = revocationRPs,
            reason = revocationReason
        )
        return signContents(contents, responder)
    }

    private fun signContents(contents: OidcResponseContent, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContent.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModel(
        requestedVcs: Map<String, VerifiableCredential>?,
        requestedIdTokens: Map<String, String>?,
        requestedSelfIssuedClaims: Map<String, String>?,
        presentationsAudience: String? = null,
        responder: Identifier,
        expiresIn: Int
    ): AttestationClaimModel? {
        if (areNoCollectedClaims(requestedVcs, requestedIdTokens, requestedSelfIssuedClaims)) {
            return null
        }
        val presentationAttestations = createPresentations(requestedVcs, presentationsAudience, responder, expiresIn)
        return AttestationClaimModel(requestedSelfIssuedClaims, requestedIdTokens, presentationAttestations)
    }

    private fun createPresentations(
        requestedVcs: Map<String, VerifiableCredential>?,
        audience: String? = null,
        responder: Identifier,
        expiresIn: Int
    ): Map<String, String>? {
        return if (audience != null) {
            requestedVcs?.mapValues {
                verifiablePresentationFormatter.createPresentation(
                    listOf(it.value),
                    audience,
                    responder,
                    expiresIn
                )
            }
        } else if (!requestedVcs.isNullOrEmpty()) {
            throw FormatterException("No audience specified for presentations")
        } else {
            null
        }
    }

    private fun areNoCollectedClaims(
        requestedVcs: Map<String, VerifiableCredential>?,
        requestedIdTokens: Map<String, String>?,
        requestedSelfIssuedClaims: Map<String, String>?
    ): Boolean {
        return (requestedVcs.isNullOrEmpty() && requestedIdTokens.isNullOrEmpty() && requestedSelfIssuedClaims.isNullOrEmpty())
    }
}