/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.identifier.models.Identifier

/**
 * Response formed from a Request.
 *
 * @param request request from which response is created
 * @param audience entity to send the response to.
 * @param responder identifies who sent the response
 */
sealed class Response(open val request: Request, val audience: String, open val responder: Identifier)

class IssuanceResponse(override val request: IssuanceRequest, override val responder: Identifier) :
    Response(request, request.contract.input.credentialIssuer, responder) {

    val requestedVcMap: MutableMap<PresentationAttestation, VerifiableCredential> = mutableMapOf()
    val requestedIdTokenMap: MutableMap<String, String> = mutableMapOf()
    val requestedSelfAttestedClaimMap: MutableMap<String, String> = mutableMapOf()
}

class PresentationResponse(override val request: PresentationRequest, override val responder: Identifier) :
    Response(request, request.content.redirectUrl, responder) {

    val requestedVcPresentationSubmissionMap: MutableMap<CredentialPresentationInputDescriptor, VerifiableCredential> = mutableMapOf()
}