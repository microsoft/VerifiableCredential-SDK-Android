/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.util.controlflow.ValidatorException

/**
 * Response formed from a Request.
 *
 * @param request request from which response is created
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, val audience: String)

class IssuanceResponse(override val request: IssuanceRequest) :
    Response(request, request.contract.input.credentialIssuer) {
    val requestedVcMap: RequestedVcMap = mutableMapOf()
    val requestedIdTokenMap: RequestedIdTokenMap = mutableMapOf()
    val requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap = mutableMapOf()
}

class PresentationResponse(override val request: PresentationRequest) :
    Response(request, request.content.clientId ?: throw ValidatorException("No audience in presentation request")) {
    val requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap = mutableMapOf()
}

typealias RequestedIdTokenMap = MutableMap<String, String>
typealias RequestedSelfAttestedClaimMap = MutableMap<String, String>
typealias RequestedVcMap = MutableMap<PresentationAttestation, VerifiableCredential>
typealias RequestedVcPresentationSubmissionMap = MutableMap<CredentialPresentationInputDescriptor, VerifiableCredential>