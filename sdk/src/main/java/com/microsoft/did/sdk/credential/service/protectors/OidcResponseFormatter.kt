/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.contexts.IdTokenContext
import com.microsoft.did.sdk.credential.service.models.contexts.SelfAttestedClaimContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiablePresentationContext
import com.microsoft.did.sdk.credential.service.models.verifiablePresentation.VerifiablePresentationContent
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
        expiryInSeconds: Int,
        verifiablePresentationContexts: Map<String, VerifiablePresentationContext>? = null,
        idTokenContexts: Map<String, IdTokenContext>? = null,
        selfAttestedClaimContexts: Map<String, SelfAttestedClaimContext>? = null,
        contract: String? = null,
        nonce: String? = null,
        state: String? = null,
        transformingVerifiableCredential: VerifiableCredential? = null,
        recipientIdentifier: String? = null
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        if (exp == null) {
            throw FormatterException("Expiry for OIDC Responses cannot be null")
        }
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val attestationResponse = this.createAttestationClaimModel(
            verifiablePresentationContexts,
            idTokenContexts,
            selfAttestedClaimContexts,
            presentationsAudience,
            responder)

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
            recipient = recipientIdentifier
        )
        return signContents(contents, responder)
    }

    private fun signContents(contents: OidcResponseContent, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContent.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModel(
        verifiablePresentationContexts: Map<String, VerifiablePresentationContext>? = null,
        idTokenContexts: Map<String, IdTokenContext>? = null,
        selfAttestedClaimContexts: Map<String, SelfAttestedClaimContext>? = null,
        presentationsAudience: String? = null,
        responder: Identifier
    ): AttestationClaimModel? {
        if (areNoCollectedClaims(verifiablePresentationContexts, idTokenContexts, selfAttestedClaimContexts)) {
            return null
        }
        val presentationAttestations = createPresentations(
            verifiablePresentationContexts,
            presentationsAudience,
            responder)
        val selfAttestedClaimMapping = selfAttestedClaimContexts?.mapValues { it.value.value }
        val idTokenMapping = idTokenContexts?.mapValues { it.value.rawToken }
        return AttestationClaimModel(selfAttestedClaimMapping, idTokenMapping, presentationAttestations)
    }

    private fun createPresentations(
        verifiablePresentationContexts: Map<String, VerifiablePresentationContext>?,
        audience: String? = null,
        responder: Identifier
    ): Map<String, String>? {
        return if (audience != null) {
            verifiablePresentationContexts?.mapValues {
                verifiablePresentationFormatter.createPresentation(
                    it.value,
                    audience,
                    responder
                )
            }
        } else if (!verifiablePresentationContexts.isNullOrEmpty()) {
            throw FormatterException("No audience specified for presentations")
        } else {
            null
        }
    }

    private fun areNoCollectedClaims(
        verifiablePresentationContexts: Map<String, VerifiablePresentationContext>? = null,
        idTokenContexts: Map<String, IdTokenContext>? = null,
        selfAttestedClaimContexts: Map<String, SelfAttestedClaimContext>? = null
    ): Boolean {
        return (verifiablePresentationContexts.isNullOrEmpty() && idTokenContexts.isNullOrEmpty() && selfAttestedClaimContexts.isNullOrEmpty())
    }
}