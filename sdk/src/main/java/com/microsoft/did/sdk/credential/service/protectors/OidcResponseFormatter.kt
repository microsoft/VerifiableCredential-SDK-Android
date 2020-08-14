/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedIdTokenMap
import com.microsoft.did.sdk.credential.service.RequestedSelfAttestedClaimMap
import com.microsoft.did.sdk.credential.service.RequestedVchPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContent
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContentForIssuance
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContentForPresentation
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationSubmission
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.controlflow.FormatterException
import com.microsoft.did.sdk.util.log.SdkLog
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
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap = mutableMapOf(),
        requestedIdTokenMap: RequestedIdTokenMap = mutableMapOf(),
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap = mutableMapOf(),
        credentialPresentationSubmission: CredentialPresentationSubmission? = null,
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
        var attestationResponse: AttestationClaimModel? = null
        if (requestedVchMap.isNotEmpty())
            attestationResponse = this.createAttestationClaimModelForIssuance(
                requestedVchMap,
                requestedIdTokenMap,
                requestedSelfAttestedClaimMap,
                presentationsAudience,
                responder
            )
        else if (requestedVchPresentationSubmissionMap.isNotEmpty())
            attestationResponse = this.createAttestationClaimModelForPresentation(
                requestedVchPresentationSubmissionMap,
                presentationsAudience,
                responder
            )
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
            presentationSubmission = credentialPresentationSubmission,
            attestations = attestationResponse,
            vc = transformingVerifiableCredential?.raw,
            recipient = recipientIdentifier
        )
        return signContents(contents, responder)
    }

    fun formatIssuanceResponse(
        responder: Identifier,
        expiryInSeconds: Int,
        requestedVchMap: RequestedVchMap = mutableMapOf(),
        issuanceResponse: IssuanceResponse
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        if (exp == null) {
            throw FormatterException("Expiry for OIDC Responses cannot be null")
        }
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        var attestationResponse = this.createAttestationClaimModelForIssuance(
                requestedVchMap,
                issuanceResponse.getRequestedIdTokens(),
                issuanceResponse.getRequestedSelfAttestedClaims(),
                issuanceResponse.request.entityIdentifier,
                responder
            )
        val contents = OidcResponseContentForIssuance(
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm),
            aud = issuanceResponse.audience,
            did = did,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            jti = jti,
            contract = issuanceResponse.request.contractUrl,
            attestations = attestationResponse
        )
        return signContentsForIssuance(contents, responder)
    }

    private fun signContents(contents: OidcResponseContent, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContent.serializer(), contents)
        SdkLog.d("serialized content is $serializedResponseContent")
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun signContentsForIssuance(contents: OidcResponseContentForIssuance, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContentForIssuance.serializer(), contents)
        SdkLog.d("serialized content is $serializedResponseContent")
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    fun formatPresentationResponse(
        responder: Identifier,
        expiryInSeconds: Int,
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap = mutableMapOf(),
        credentialPresentationSubmission: CredentialPresentationSubmission? = null,
        presentationResponse: PresentationResponse
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        if (exp == null) {
            throw FormatterException("Expiry for OIDC Responses cannot be null")
        }
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        var attestationResponse = this.createAttestationClaimModelForPresentation(
                requestedVchPresentationSubmissionMap,
                presentationResponse.request.entityIdentifier,
                responder
            )
        val contents = OidcResponseContentForPresentation(
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm),
            aud = presentationResponse.audience,
            nonce = presentationResponse.request.content.nonce,
            did = did,
            subJwk = key.toJWK(),
            iat = iat,
            exp = exp,
            state = presentationResponse.request.content.state,
            jti = jti,
            presentationSubmission = credentialPresentationSubmission,
            attestations = attestationResponse
        )
        return signContentsForPresentation(contents, responder)
    }

    private fun signContentsForPresentation(contents: OidcResponseContentForPresentation, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContentForPresentation.serializer(), contents)
        SdkLog.d("serialized content is $serializedResponseContent")
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModelForIssuance(
        requestedVchMap: RequestedVchMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel? {
        if (areNoCollectedClaims(requestedVchMap, requestedIdTokenMap, requestedSelfAttestedClaimMap)) {
            return null
        }
        val presentationAttestations = createPresentationsForIssuance(
            requestedVchMap,
            presentationsAudience,
            responder
        )
        val nullableSelfAttestedClaimRequestMapping = if (requestedSelfAttestedClaimMap.isEmpty()) {
            null
        } else {
            requestedSelfAttestedClaimMap
        }
        val nullableIdTokenRequestMapping = if (requestedIdTokenMap.isEmpty()) {
            null
        } else {
            requestedIdTokenMap
        }
        return AttestationClaimModel(nullableSelfAttestedClaimRequestMapping, nullableIdTokenRequestMapping, presentationAttestations)
    }

    private fun createAttestationClaimModelForPresentation(
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel? {
        if (requestedVchPresentationSubmissionMap.isNullOrEmpty()) {
            return null
        }
        val presentationAttestations = createPresentationsForPresentation(
            requestedVchPresentationSubmissionMap,
            presentationsAudience,
            responder
        )
        return AttestationClaimModel(null, null, presentationAttestations)
    }

    private fun createPresentationsForIssuance(
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

    private fun createPresentationsForPresentation(
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap,
        audience: String,
        responder: Identifier
    ): Map<String, String>? {
        val vpMap = requestedVchPresentationSubmissionMap.map { (key, value) ->
            key.id to verifiablePresentationFormatter.createPresentation(
                value.verifiableCredential,
                Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS,
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