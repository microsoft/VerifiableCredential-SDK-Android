/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedIdTokenMap
import com.microsoft.did.sdk.credential.service.RequestedSelfAttestedClaimMap
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.RequestedVchPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.ExchangeRequest
import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContentForExchange
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContentForIssuance
import com.microsoft.did.sdk.credential.service.models.oidc.OidcResponseContentForPresentation
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationSubmission
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationSubmissionDescriptor
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants.CREDENTIAL_PATH_IN_RESPONSE
import com.microsoft.did.sdk.util.Constants.CREDENTIAL_PRESENTATION_ENCODING
import com.microsoft.did.sdk.util.Constants.CREDENTIAL_PRESENTATION_FORMAT
import com.microsoft.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
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
    fun formatIssuanceResponse(
        responder: Identifier,
        expiryInSeconds: Int,
        requestedVchMap: RequestedVchMap = mutableMapOf(),
        issuanceResponse: IssuanceResponse
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val attestationResponse = this.createAttestationClaimModelForIssuance(
            requestedVchMap,
            issuanceResponse.getRequestedIdTokens(),
            issuanceResponse.getRequestedSelfAttestedClaims(),
            issuanceResponse.request.entityIdentifier,
            responder
        )
        return createAndSignOidcResponseContentForIssuance(responder, issuanceResponse, did, iat, exp, jti, attestationResponse)
    }

    private fun createAndSignOidcResponseContentForIssuance(
        responder: Identifier,
        issuanceResponse: IssuanceResponse,
        did: String,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String,
        attestationResponse: AttestationClaimModel
    ): String {
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = OidcResponseContentForIssuance(
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm),
            aud = issuanceResponse.audience,
            did = did,
            publicKeyJwk = key.toJWK(),
            responseCreationTime = issuedTime,
            expirationTime = expiryTime,
            responseId = responseId
        )
        contents.contract = issuanceResponse.request.contractUrl
        contents.attestations = attestationResponse
        return signContentsForIssuance(contents, responder)
    }

    private fun signContentsForIssuance(contents: OidcResponseContentForIssuance, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContentForIssuance.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    fun formatPresentationResponse(
        responder: Identifier,
        expiryInSeconds: Int,
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap = mutableMapOf(),
        presentationResponse: PresentationResponse
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        val jti = UUID.randomUUID().toString()
        val did = responder.id
        val attestationResponse = this.createAttestationClaimModelForPresentation(
            requestedVchPresentationSubmissionMap,
            presentationResponse.request.entityIdentifier,
            responder
        )
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.getRequestedVchClaims().map {
                CredentialPresentationSubmissionDescriptor(
                    it.component1().id,
                    "$CREDENTIAL_PATH_IN_RESPONSE.${it.component1().id}",
                    CREDENTIAL_PRESENTATION_FORMAT,
                    CREDENTIAL_PRESENTATION_ENCODING
                )
            }
        val credentialPresentationSubmission = CredentialPresentationSubmission(credentialPresentationSubmissionDescriptors)
        return createAndSignOidcResponseContentForPresentation(
            responder,
            presentationResponse,
            did,
            iat,
            exp,
            jti,
            attestationResponse,
            credentialPresentationSubmission
        )
    }

    private fun createAndSignOidcResponseContentForPresentation(
        responder: Identifier,
        presentationResponse: PresentationResponse,
        did: String,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String,
        attestationResponse: AttestationClaimModel,
        credentialPresentationSubmission: CredentialPresentationSubmission
    ): String {
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = OidcResponseContentForPresentation(
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm),
            aud = presentationResponse.audience,
            nonce = presentationResponse.request.content.nonce,
            did = did,
            publicKeyJwk = key.toJWK(),
            responseCreationTime = issuedTime,
            expirationTime = expiryTime,
            state = presentationResponse.request.content.state,
            responseId = responseId,
            presentationSubmission = credentialPresentationSubmission,
            attestations = attestationResponse
        )
        return signContentsForPresentation(contents, responder)
    }

    private fun signContentsForPresentation(contents: OidcResponseContentForPresentation, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContentForPresentation.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    fun formatExchangeResponse(
        requester: Identifier,
        expiryInSeconds: Int,
        exchangeRequest: ExchangeRequest
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        val jti = UUID.randomUUID().toString()
        val did = requester.id
        return createAndSignOidcResponseContentForExchange(requester, exchangeRequest, did, iat, exp, jti)
    }

    private fun createAndSignOidcResponseContentForExchange(
        requester: Identifier,
        exchangeRequest: ExchangeRequest,
        did: String,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String
    ): String {
        val key = cryptoOperations.keyStore.getPublicKey(requester.signatureKeyReference).getKey()
        val contents = OidcResponseContentForExchange(
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm),
            aud = exchangeRequest.audience,
            did = did,
            publicKeyJwk = key.toJWK(),
            responseCreationTime = issuedTime,
            expirationTime = expiryTime,
            responseId = responseId,
            vc = exchangeRequest.verifiableCredential?.raw,
            recipient = exchangeRequest.pairwiseDid
        )
        return signContentsForExchange(contents, requester)
    }

    private fun signContentsForExchange(contents: OidcResponseContentForExchange, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(OidcResponseContentForExchange.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModelForIssuance(
        requestedVchMap: RequestedVchMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel {
        if (areNoCollectedClaims(requestedVchMap, requestedIdTokenMap, requestedSelfAttestedClaimMap)) {
            return AttestationClaimModel()
        }
        val presentationAttestations = createPresentations(
            requestedVchMap.map { (key, value) ->
                Pair(key.credentialType, key.validityInterval) to value
            }.toMap(),
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
    ): AttestationClaimModel {
        if (requestedVchPresentationSubmissionMap.isNullOrEmpty()) {
            return AttestationClaimModel()
        }
        val presentationAttestations = createPresentations(
            requestedVchPresentationSubmissionMap.map { (key, value) ->
                Pair(key.id, DEFAULT_VP_EXPIRATION_IN_SECONDS) to value
            }.toMap(),
            presentationsAudience,
            responder
        )
        return AttestationClaimModel(null, null, presentationAttestations)
    }

    private fun createPresentations(
        requestedVcIdToVchMap: RequestedVcIdToVchMap,
        audience: String,
        responder: Identifier
    ): Map<String, String>? {
        val vpMap = requestedVcIdToVchMap.map { (key, value) ->
            key.first to verifiablePresentationFormatter.createPresentation(
                value.verifiableCredential,
                key.second,
                audience,
                responder
            )
        }.toMap()
        return if (vpMap.isEmpty())
            null
        else
            vpMap
    }

    private fun areNoCollectedClaims(
        requestedVchMap: RequestedVchMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap
    ): Boolean {
        return (requestedVchMap.isNullOrEmpty() && requestedIdTokenMap.isNullOrEmpty() && requestedSelfAttestedClaimMap.isNullOrEmpty())
    }
}

typealias RequestedVcIdToVchMap = Map<Pair<String, Int>, VerifiableCredentialHolder>