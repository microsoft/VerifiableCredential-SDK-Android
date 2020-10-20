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
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
import com.microsoft.did.sdk.util.log.SdkLog
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
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap = mutableMapOf(),
        presentationResponse: PresentationResponse,
        responder: Identifier,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): String {
        var startLoad = System.nanoTime()

        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val responseId = UUID.randomUUID().toString()
        var loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - createIssuedAndExpiryTime: ${loadTimeInMs}ms")

        startLoad = System.nanoTime()
        val (attestationResponse, credentialPresentationSubmission) = createAttestationsAndPresentationSubmission(
            requestedVcPresentationSubmissionMap,
            presentationResponse,
            responder
        )
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - createAttestationsAndPresentationSubmission: ${loadTimeInMs}ms")

        startLoad = System.nanoTime()
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - cryptoOperations.keyStore.getPublicKey: ${loadTimeInMs}ms")

        startLoad = System.nanoTime()
        val oidcResponseClaims = PresentationResponseClaims(credentialPresentationSubmission, attestationResponse).apply {
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

        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - PresentationResponseClaims: ${loadTimeInMs}ms")
        return signContents(oidcResponseClaims, responder)
    }

    private fun createAttestationsAndPresentationSubmission(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        presentationResponse: PresentationResponse,
        responder: Identifier
    ): Pair<AttestationClaimModel, PresentationSubmission> {
        var startLoad = System.nanoTime()
        val attestationResponse = this.createAttestationClaimModel(
            requestedVcPresentationSubmissionMap,
            presentationResponse.request.entityIdentifier,
            responder
        )
        var loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - createAttestationClaimModel: ${loadTimeInMs}ms")

        startLoad = System.nanoTime()
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.requestedVcPresentationSubmissionMap.map {
                PresentationSubmissionDescriptor(
                    it.key.id,
                    "${Constants.CREDENTIAL_PATH_IN_RESPONSE}.${it.key.id}",
                    Constants.CREDENTIAL_PRESENTATION_FORMAT,
                    Constants.CREDENTIAL_PRESENTATION_ENCODING
                )
            }
        loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - requestedVcPresentationSubmissionMap: ${loadTimeInMs}ms")
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
        val startLoad = System.nanoTime()
        val serializedResponseContent = serializer.stringify(PresentationResponseClaims.serializer(), contents)
        val signedStr = signer.signWithIdentifier(serializedResponseContent, responder)
        val loadTimeInMs = (System.nanoTime() - startLoad) / 1000000
        SdkLog.v("Perf - signContents: ${loadTimeInMs}ms")
        return signedStr
    }
}