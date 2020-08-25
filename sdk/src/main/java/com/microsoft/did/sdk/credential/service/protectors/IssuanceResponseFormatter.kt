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
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        val jti = UUID.randomUUID().toString()
        val attestationResponse = this.createAttestationClaimModel(
            requestedVchMap,
            issuanceResponse.getRequestedIdTokens(),
            issuanceResponse.getRequestedSelfAttestedClaims(),
            issuanceResponse.request.entityIdentifier,
            issuanceResponse.responder
        )
        return createAndSignOidcResponseContent(issuanceResponse, iat, exp, jti, attestationResponse)
    }

    private fun createAndSignOidcResponseContent(
        issuanceResponse: IssuanceResponse,
        issuedTime: Long,
        expiryTime: Long,
        jti: String,
        attestationResponse: AttestationClaimModel
    ): String {
        val responder = issuanceResponse.responder
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = IssuanceResponseClaims(issuanceResponse.request.contractUrl, attestationResponse).apply {
            sub = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            aud = issuanceResponse.audience
            did = responder.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            expirationTime = expiryTime
            responseId = jti
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
        val presentationAttestations = createPresentations(
            requestedVchMap.map { (key, value) ->
                Pair(key.credentialType, key.validityInterval) to value
            }.toMap(),
            presentationsAudience,
            responder
        )
        return AttestationClaimModel(requestedSelfAttestedClaimMap, requestedIdTokenMap, presentationAttestations)
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