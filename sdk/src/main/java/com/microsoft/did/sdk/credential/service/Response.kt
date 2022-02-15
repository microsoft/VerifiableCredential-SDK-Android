/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.pin.IssuancePin
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
    var issuancePin: IssuancePin? = null
    val requestedVcMap: RequestedVcMap = mutableMapOf()
    val requestedIdTokenMap: RequestedIdTokenMap = mutableMapOf()
    val requestedAccessTokenMap: RequestedAccessTokenMap = mutableMapOf()
    val requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap = mutableMapOf()
}

class PresentationResponse(override val request: PresentationRequest) :
    Response(request, request.content.clientId) {
    val requestedVcPresentationSubmissionMap: RequestedVcPresentationSubmissionMap = mutableMapOf()
    val requestedVcPresentationDefinitionId: String = request.getPresentationDefinition().id
}

typealias RequestedIdTokenMap = MutableMap<String, String>
typealias RequestedAccessTokenMap = MutableMap<String, String>
typealias RequestedSelfAttestedClaimMap = MutableMap<String, String>
typealias RequestedVcMap = MutableMap<PresentationAttestation, VerifiableCredential>
typealias RequestedVcPresentationSubmissionMap = MutableMap<CredentialPresentationInputDescriptor, VerifiableCredential>