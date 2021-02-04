/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVcPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.oidc.AttestationClaimModel
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmissionDescriptor
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationResponseFormatter @Inject constructor(
    private val serializer: Json,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter,
    private val signer: TokenSigner,
    private val keyStore: EncryptedKeyStore
) {
    fun formatResponse(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap = mutableMapOf(),
        presentationResponse: PresentationResponse,
        responder: Identifier,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): String {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responseId = UUID.randomUUID().toString()
        val (attestationResponse, credentialPresentationSubmission) = createAttestationsAndPresentationSubmission(
            requestedVcPresentationSubmissionMap,
            presentationResponse,
            responder
        )
        val key = keyStore.getKey(responder.signatureKeyReference)

        val oidcResponseClaims = PresentationResponseClaims(credentialPresentationSubmission, attestationResponse).apply {
            publicKeyThumbPrint = key.computeThumbprint().toString()
            audience = presentationResponse.audience
            nonce = presentationResponse.request.content.nonce
            did = responder.id
            publicKeyJwk = key.toPublicJWK()
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
            state = presentationResponse.request.content.state
            this.responseId = responseId
        }
        return signContents(oidcResponseClaims, responder)
    }

    private fun createAttestationsAndPresentationSubmission(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        presentationResponse: PresentationResponse,
        responder: Identifier
    ): Pair<AttestationClaimModel, PresentationSubmission> {
        val attestationResponse = this.createAttestationClaimModel(
            requestedVcPresentationSubmissionMap,
            presentationResponse.request.entityIdentifier,
            responder
        )
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.requestedVcPresentationSubmissionMap.map {
                PresentationSubmissionDescriptor(
                    it.key.id,
                    "${Constants.CREDENTIAL_PATH_IN_RESPONSE}.${it.key.id}",
                    Constants.CREDENTIAL_PRESENTATION_FORMAT,
                    Constants.CREDENTIAL_PRESENTATION_ENCODING
                )
            }
        val credentialPresentationSubmission = PresentationSubmission(credentialPresentationSubmissionDescriptors)
        return Pair(attestationResponse, credentialPresentationSubmission)
    }

    private fun createAttestationClaimModel(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        presentationsAudience: String,
        responder: Identifier
    ): AttestationClaimModel {
        if (requestedVcPresentationSubmissionMap.isNullOrEmpty()) {
            return AttestationClaimModel()
        }
        val presentationAttestations = createPresentations(requestedVcPresentationSubmissionMap, presentationsAudience, responder)
        return AttestationClaimModel(presentations = presentationAttestations)
    }

    private fun createPresentations(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        audience: String,
        responder: Identifier
    ): Map<String, String> {
        return requestedVcPresentationSubmissionMap.map { (inputDescriptor, vc) ->
            inputDescriptor.id to verifiablePresentationFormatter.createPresentation(
                vc,
                DEFAULT_VP_EXPIRATION_IN_SECONDS,
                audience,
                responder
            )
        }.toMap()
    }

    private fun signContents(contents: PresentationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.encodeToString(PresentationResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}