/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service.protectors

import com.microsoft.did.sdk.credential.service.PresentationResponse
import com.microsoft.did.sdk.credential.service.RequestedVcPresentationSubmissionMap
import com.microsoft.did.sdk.credential.service.models.oidc.PresentationResponseClaims
import com.microsoft.did.sdk.credential.service.models.oidc.VpTokenInResponse
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmission
import com.microsoft.did.sdk.credential.service.models.presentationexchange.PresentationSubmissionDescriptor
import com.microsoft.did.sdk.identifier.models.Identifier
import com.microsoft.did.sdk.util.Constants
import com.microsoft.did.sdk.util.Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS
import com.microsoft.did.sdk.util.controlflow.ValidatorException
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresentationResponseFormatter @Inject constructor(
    private val serializer: Json,
    private val verifiablePresentationFormatter: VerifiablePresentationFormatter,
    private val signer: TokenSigner
    ) {
    fun formatResponse(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap = mutableMapOf(),
        presentationResponse: PresentationResponse,
        responder: Identifier,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): Pair<String, String> {
        val (issuedTime, expiryTime) = createIssuedAndExpiryTime(expiryInSeconds)
        val credentialPresentationSubmission = createAttestationsAndPresentationSubmission(presentationResponse)

        val oidcResponseClaims = PresentationResponseClaims(VpTokenInResponse(credentialPresentationSubmission)).apply {
            subject = responder.id
            audience = presentationResponse.audience
            nonce = presentationResponse.request.content.nonce
            responseCreationTime = issuedTime
            responseExpirationTime = expiryTime
        }

        val attestationResponse = createPresentations(
            requestedVcPresentationSubmissionMap,
            presentationResponse.request.content.clientId,
            responder,
            presentationResponse.request.content.nonce
        )

        val idToken = signContents(oidcResponseClaims, responder)
        return Pair(idToken, attestationResponse)
    }

    private fun createAttestationsAndPresentationSubmission(presentationResponse: PresentationResponse): PresentationSubmission {
        presentationResponse.requestedVcPresentationSubmissionMap.entries.indices
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.requestedVcPresentationSubmissionMap.map { pair ->
                PresentationSubmissionDescriptor(
                    pair.key.id,
                    Constants.VERIFIABLE_PRESENTATION_FORMAT,
                    "$",
                    PresentationSubmissionDescriptor(
                        pair.key.id,
                        Constants.VERIFIABLE_CREDENTIAL_FORMAT,
                        "${Constants.CREDENTIAL_PATH_IN_RESPONSE}${
                            presentationResponse.requestedVcPresentationSubmissionMap.toList().indexOf(Pair(pair.key, pair.value))
                        }]"
                    )
                )
            }
        val presentationSubmissionId = UUID.randomUUID().toString()
        return PresentationSubmission(
            presentationSubmissionId,
            presentationResponse.requestedVcPresentationDefinitionId,
            credentialPresentationSubmissionDescriptors
        )
    }

    private fun createPresentations(
        requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap,
        audience: String,
        responder: Identifier,
        nonce: String
    ): String {
        return verifiablePresentationFormatter.createPresentation(
            requestedVcPresentationSubmissionMap.values.toList(),
            DEFAULT_VP_EXPIRATION_IN_SECONDS,
            audience,
            responder,
            nonce
        )
    }

    private fun signContents(contents: PresentationResponseClaims, responder: Identifier): String {
        val serializedResponseContent = serializer.encodeToString(PresentationResponseClaims.serializer(), contents)
        return signer.signWithIdentifier(serializedResponseContent, responder)
    }
}