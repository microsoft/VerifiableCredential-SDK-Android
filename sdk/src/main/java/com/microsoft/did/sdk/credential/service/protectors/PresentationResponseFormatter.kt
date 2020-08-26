/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVchPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmissionDescriptor
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
import com.microsoft.did.sdk.util.serializer.Serializer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationResponseFormatter @Inject constructor(
    private val cryptoOperations: CryptoOperations,
    private val serializer: Serializer,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter,
    private val signer: TokenSigner
) {
    fun formatResponse(
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap = mutableMapOf(),
        presentationResponse: PresentationResponse,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responseId = UUID.randomUUID().toString()
        val attestationResponse = this.createAttestationClaimModel(
            requestedVchPresentationSubmissionMap,
            presentationResponse.request.entityIdentifier,
            presentationResponse.responder
        )
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.getRequestedVchClaims().map {
                PresentationSubmissionDescriptor(
                    it.key.id,
                    "${Constants.CREDENTIAL_PATH_IN_RESPONSE}.${it.key.id}",
                    Constants.CREDENTIAL_PRESENTATION_FORMAT,
                    Constants.CREDENTIAL_PRESENTATION_ENCODING
                )
            }
        val credentialPresentationSubmission = PresentationSubmission(credentialPresentationSubmissionDescriptors)
        return createAndSignOidcResponseContent(
            presentationResponse,
            issuedTime,
            expiryTime,
            responseId,
            attestationResponse,
            credentialPresentationSubmission
        )
    }

    private fun createAndSignOidcResponseContent(
        presentationResponse: PresentationResponse,
        issuedTime: Long,
        expiryTime: Long,
        responseId: String,
        attestationResponse: AttestationClaimModel,
        presentationSubmission: PresentationSubmission
    ): String {
        val responder = presentationResponse.responder
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = PresentationResponseClaims(presentationSubmission, attestationResponse).apply {
            publicKeyThumbPrint = key.getThumbprint(cryptoOperations, Sha.SHA256.algorithm)
            audience = presentationResponse.audience
            nonce = presentationResponse.request.content.nonce
            did = responder.id
            publicKeyJwk = key.toJWK()
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
            state = presentationResponse.request.content.state
            this.responseId = responseId
        }
        return signContents(contents, responder)
    }

    private fun signContents(contents: PresentationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.stringify(PresentationResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }

    private fun createAttestationClaimModel(
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel {
        if (requestedVchPresentationSubmissionMap.isNullOrEmpty()) {
            return AttestationClaimModel()
        }
        val presentationAttestations = createPresentations(requestedVchPresentationSubmissionMap, presentationsAudience, responder)
        return AttestationClaimModel(presentations = presentationAttestations)
    }

    private fun createPresentations(
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap,
        audience: String,
        responder: Identifier
    ): Map<String, String> {
        return requestedVchPresentationSubmissionMap.map { (key, value) ->
            key.id to verifiablePresentationFormatter.createPresentation(
                value.verifiableCredential,
                DEFAULT_VP_EXPIRATION_IN_SECONDS,
                audience,
                responder
            )
        }.toMap()
    }
}