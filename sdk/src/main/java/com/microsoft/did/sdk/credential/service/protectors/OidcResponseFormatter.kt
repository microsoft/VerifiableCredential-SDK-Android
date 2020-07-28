/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.service.RequestedIdTokenMap
import com.microsoft.did.sdk.credential.service.RequestedSelfAttestedClaimMap
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.controlflow.FormatterException
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.UUID
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
        presentationsAudience: String = "",
        expiryInSeconds: Int,
        requestedVchMap: RequestedVchMap = mutableMapOf(),
        requestedIdTokenMap: RequestedIdTokenMap = mutableMapOf(),
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap = mutableMapOf(),
        contract: String? = null,
        nonce: String? = null,
        state: String? = null,
        transformingVerifiableCredential: VerifiableCredential? = null,
        recipientIdentifier: String? = null,
        revocationRPs: List<String>? = null,
        revocationReason: String = ""
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        if (exp == null) {
            throw FormatterException("Expiry for OIDC Responses cannot be null")
        }
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val attestationResponse = this.createAttestationClaimModel(
            requestedVchMap,
            requestedIdTokenMap,
            requestedSelfAttestedClaimMap,
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
            recipient = recipientIdentifier,
            rpList = revocationRPs,
            reason = revocationReason
        )
        return signContents(contents, responder)
    }

    private fun signContents(contents: OidcResponseContent, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContent.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModel(
        requestedVchMap: RequestedVchMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel? {
        if (areNoCollectedClaims(requestedVchMap, requestedIdTokenMap, requestedSelfAttestedClaimMap)) {
            return null
        }
        val presentationAttestations = createPresentations(
            requestedVchMap,
            presentationsAudience,
            responder)
        val nullableSelfAttestedClaimRequestMapping = if (requestedSelfAttestedClaimMap.isEmpty()) { null } else { requestedSelfAttestedClaimMap }
        val nullableIdTokenRequestMapping = if (requestedIdTokenMap.isEmpty()) { null } else { requestedIdTokenMap }
        return AttestationClaimModel(nullableSelfAttestedClaimRequestMapping, nullableIdTokenRequestMapping, presentationAttestations)
    }

    private fun createPresentations(
        requestedVchMap: RequestedVchMap,
        audience: String,
        responder: Identifier
    ): Map<String, String>? {
        val vpMap = requestedVchMap.map { (key, value) ->
            key.credentialType to verifiablePresentationFormatter.createPresentation(
                value.verifiableCredential,
                key.validityInterval,
                audience,
                responder
            )
        }.toMap()
        return if (vpMap.isEmpty()) {
            null
        } else {
            vpMap
        }
    }

    private fun areNoCollectedClaims(
        requestedVchMap: RequestedVchMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap
    ): Boolean {
        return (requestedVchMap.isNullOrEmpty() && requestedIdTokenMap.isNullOrEmpty() && requestedSelfAttestedClaimMap.isNullOrEmpty())
    }
}