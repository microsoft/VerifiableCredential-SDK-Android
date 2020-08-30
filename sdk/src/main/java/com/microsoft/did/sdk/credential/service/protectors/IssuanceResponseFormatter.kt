/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.IssuanceResponse
import com.microsoft.did.sdk.credential.service.RequestedIdTokenMap
import com.microsoft.did.sdk.credential.service.RequestedSelfAttestedClaimMap
import com.microsoft.did.sdk.credential.service.RequestedVchMap
import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.IssuanceResponseClaims
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IssuanceResponseFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Serializer,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter,
    private val signer: TokenSigner
) {

    fun formatResponse(
        requestedVchMap: RequestedVchMap = mutableMapOf(),
        issuanceResponse: IssuanceResponse,
        expiryInSeconds: Int
    ): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responseId = UUID.randomUUID().toString()
        val attestationResponse = this.createAttestationClaimModel(
            requestedVchMap,
            issuanceResponse.requestedIdTokenMap,
            issuanceResponse.requestedSelfAttestedClaimMap,
            issuanceResponse.request.entityIdentifier,
            issuanceResponse.responder
        )
        return createAndSignOidcResponseContent(issuanceResponse, issuedTime, expiryTime, responseId, attestationResponse)
    }

    private fun createAndSignOidcResponseContent(
        issuanceResponse: IssuanceResponse,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String,
        attestationResponse: AttestationClaimModel
    ): String {
        val responder = issuanceResponse.responder
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = IssuanceResponseClaims(issuanceResponse.request.contractUrl, attestationResponse).apply {
            publicKeyThumbPrint = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            audience = issuanceResponse.audience
            did = responder.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
            this.responseId = responseId
        }
        return signContents(contents, responder)
    }

    private fun signContents(contents: IssuanceResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(IssuanceResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModel(
        requestedVchMap: RequestedVchMap,
        requestedIdTokenMap: RequestedIdTokenMap,
        requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel {
        if (areNoCollectedClaims(requestedVchMap, requestedIdTokenMap, requestedSelfAttestedClaimMap)) {
            return AttestationClaimModel()
        }
        val presentationAttestations = createPresentations(requestedVchMap, presentationsAudience, responder)
        return AttestationClaimModel(requestedSelfAttestedClaimMap, requestedIdTokenMap, presentationAttestations)
    }

    private fun createPresentations(requestedVchMap: RequestedVchMap, audience: String, responder: Identifier): Map<String, String> {
        return requestedVchMap.map { (key, value) ->
            key.credentialType to verifiablePresentationFormatter.createPresentation(
                value.verifiableCredential,
                key.validityInterval,
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