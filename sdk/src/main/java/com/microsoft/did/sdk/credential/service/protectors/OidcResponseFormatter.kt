/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.requestMappings.VerifiableCredentialRequestMapping
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
        presentationsAudience: String = "",
        expiryInSeconds: Int,
        verifiableCredentialRequestMappings: List<VerifiableCredentialRequestMapping> = emptyList(),
        idTokenRequestMapping: Map<String, String> = emptyMap(),
        selfAttestedClaimRequestMapping: Map<String, String> = emptyMap(),
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
            verifiableCredentialRequestMappings,
            idTokenRequestMapping,
            selfAttestedClaimRequestMapping,
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
        verifiableCredentialRequestMappings: List<VerifiableCredentialRequestMapping>,
        idTokenRequestMapping: Map<String, String>,
        selfAttestedClaimRequestMapping: Map<String, String>,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel? {
        if (areNoCollectedClaims(verifiableCredentialRequestMappings, idTokenRequestMapping, selfAttestedClaimRequestMapping)) {
            return null
        }
        val presentationAttestations = createPresentations(
            verifiableCredentialRequestMappings,
            presentationsAudience,
            responder)
        val nullableSelfAttestedClaimRequestMapping = if (selfAttestedClaimRequestMapping.isEmpty()) { null } else { selfAttestedClaimRequestMapping }
        val nullableIdTokenRequestMapping = if (idTokenRequestMapping.isEmpty()) { null } else { idTokenRequestMapping }
        return AttestationClaimModel(nullableSelfAttestedClaimRequestMapping, nullableIdTokenRequestMapping, presentationAttestations)
    }

    private fun createPresentations(
        verifiableCredentialRequestMappings: List<VerifiableCredentialRequestMapping>,
        audience: String,
        responder: Identifier
    ): Map<String, String>? {
        val verifiablePresentationMapping = mutableMapOf<String, String>()
        verifiableCredentialRequestMappings.forEach {
            verifiablePresentationMapping[it.presentationAttestation.credentialType] = verifiablePresentationFormatter.createPresentation(
                it,
                audience,
                responder
            )
        }
        return if (verifiablePresentationMapping.isEmpty()) {
            null
        } else {
            verifiablePresentationMapping
        }
    }

    private fun areNoCollectedClaims(
        verifiableCredentialRequestMappings: List<VerifiableCredentialRequestMapping>,
        idTokenRequestMapping: Map<String, String>,
        selfAttestedClaimRequestMapping: Map<String, String>
    ): Boolean {
        return (verifiableCredentialRequestMappings.isNullOrEmpty() && idTokenRequestMapping.isNullOrEmpty() && selfAttestedClaimRequestMapping.isNullOrEmpty())
    }
}