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
import com.microsoft.did.sdk.credential.service.models.oidc.ExchangeResponseClaims
import com.microsoft.did.sdk.credential.service.models.oidc.IssuanceResponseClaims
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmissionDescriptor
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
        val attestationResponse = this.createAttestationClaimModelForIssuance(
            requestedVchMap,
            issuanceResponse.getRequestedIdTokens(),
            issuanceResponse.getRequestedSelfAttestedClaims(),
            issuanceResponse.request.entityIdentifier,
            responder
        )
        return createAndSignOidcResponseContentForIssuance(responder, issuanceResponse, iat, exp, jti, attestationResponse)
    }

    private fun createAndSignOidcResponseContentForIssuance(
        responder: Identifier,
        issuanceResponse: IssuanceResponse,
        issuedTime: Long,
        expiryTime: Long,
        jti: String,
        attestationResponse: AttestationClaimModel
    ): String {
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = IssuanceResponseClaims(
            issuanceResponse.request.contractUrl,
            attestationResponse
        ).apply {
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            aud = issuanceResponse.audience
            did = responder.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            expirationTime = expiryTime
            responseId = jti
        }
        return signContentsForIssuance(contents, responder)
    }

    private fun signContentsForIssuance(contents: IssuanceResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(IssuanceResponseClaims.serializer(), contents)
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
        val attestationResponse = this.createAttestationClaimModelForPresentation(
            requestedVchPresentationSubmissionMap,
            presentationResponse.request.entityIdentifier,
            responder
        )
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.getRequestedVchClaims().map {
                PresentationSubmissionDescriptor(
                    it.component1().id,
                    "$CREDENTIAL_PATH_IN_RESPONSE.${it.component1().id}",
                    CREDENTIAL_PRESENTATION_FORMAT,
                    CREDENTIAL_PRESENTATION_ENCODING
                )
            }
        val credentialPresentationSubmission = PresentationSubmission(credentialPresentationSubmissionDescriptors)
        return createAndSignOidcResponseContentForPresentation(
            responder,
            presentationResponse,
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
        issuedTime: Long,
        expiryTime: Long,
        jti: String,
        attestationResponse: AttestationClaimModel,
        presentationSubmission: PresentationSubmission
    ): String {
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = PresentationResponseClaims(
            presentationSubmission = presentationSubmission,
            attestations = attestationResponse
        ).apply {
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            aud = presentationResponse.audience
            nonce = presentationResponse.request.content.nonce
            did = responder.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            expirationTime = expiryTime
            state = presentationResponse.request.content.state
            responseId = jti
        }
        return signContentsForPresentation(contents, responder)
    }

    private fun signContentsForPresentation(contents: PresentationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(PresentationResponseClaims.serializer(), contents)
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
        return createAndSignOidcResponseContentForExchange(requester, exchangeRequest, iat, exp, jti)
    }

    private fun createAndSignOidcResponseContentForExchange(
        requester: Identifier,
        exchangeRequest: ExchangeRequest,
        issuedTime: Long,
        expiryTime: Long,
        jti: String
    ): String {
        val key = cryptoOperations.keyStore.getPublicKey(requester.signatureKeyReference).getKey()
        val contents = ExchangeResponseClaims(
            vc = exchangeRequest.verifiableCredential?.raw,
            recipient = exchangeRequest.pairwiseDid
        ).apply {
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            aud = exchangeRequest.audience
            did = requester.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            expirationTime = expiryTime
            responseId = jti
        }
        return signContentsForExchange(contents, requester)
    }

    private fun signContentsForExchange(contents: ExchangeResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(ExchangeResponseClaims.serializer(), contents)
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
        return AttestationClaimModel(requestedSelfAttestedClaimMap, requestedIdTokenMap, presentationAttestations)
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
        return AttestationClaimModel(presentations = presentationAttestations)
    }

    private fun createPresentations(
        requestedVcIdToVchMap: RequestedVcIdToVchMap,
        audience: String,
        responder: Identifier
    ): Map<String, String> {
        return requestedVcIdToVchMap.map { (key, value) ->
            key.first to verifiablePresentationFormatter.createPresentation(
                value.verifiableCredential,
                key.second,
                audience,
                responder
            )
        }.toMap()
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