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
) : OidcResponseFormatter {
    fun formatPresentationResponse(
        requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap = mutableMapOf(),
        presentationResponse: PresentationResponse,
        expiryInSeconds: Int = Constants.DEFAULT_EXPIRATION_IN_SECONDS
    ): String {
        val (iat, exp) = createIatAndExp(expiryInSeconds)
        val jti = UUID.randomUUID().toString()
        val attestationResponse = this.createAttestationClaimModelForPresentation(
            requestedVchPresentationSubmissionMap,
            presentationResponse.request.entityIdentifier,
            presentationResponse.responder
        )
        val credentialPresentationSubmissionDescriptors =
            presentationResponse.getRequestedVchClaims().map {
                PresentationSubmissionDescriptor(
                    it.component1().id,
                    "${Constants.CREDENTIAL_PATH_IN_RESPONSE}.${it.component1().id}",
                    Constants.CREDENTIAL_PRESENTATION_FORMAT,
                    Constants.CREDENTIAL_PRESENTATION_ENCODING
                )
            }
        val credentialPresentationSubmission = PresentationSubmission(credentialPresentationSubmissionDescriptors)
        return createAndSignOidcResponseContentForPresentation(
            presentationResponse,
            iat,
            exp,
            jti,
            attestationResponse,
            credentialPresentationSubmission
        )
    }

    private fun createAndSignOidcResponseContentForPresentation(
        presentationResponse: PresentationResponse,
        issuedTime: Long,
        expiryTime: Long,
        jti: String,
        attestationResponse: AttestationClaimModel,
        presentationSubmission: PresentationSubmission
    ): String {
        val responder = presentationResponse.responder
        val key = cryptoOperations.keyStore.getPublicKey(responder.signatureKeyReference).getKey()
        val contents = PresentationResponseClaims(presentationSubmission, attestationResponse).apply {
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
                Pair(key.id, Constants.DEFAULT_VP_EXPIRATION_IN_SECONDS) to value
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

}