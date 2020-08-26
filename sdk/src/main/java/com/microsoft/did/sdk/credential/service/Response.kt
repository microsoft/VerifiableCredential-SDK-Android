/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.presentationexchange.CredentialPresentationInputDescriptor
import com.microsoft.did.sdk.identifier.models.Identifier

/**
 * Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, val audience: String, open val responder: Identifier)
class IssuanceResponse(override val request: IssuanceRequest, override val responder: Identifier) : Response(request, request.contract.input.credentialIssuer, responder) {
    private var requestedVchMap: RequestedVchMap = mutableMapOf()
    private val requestedIdTokenMap: RequestedIdTokenMap = mutableMapOf()
    private val requestedSelfAttestedClaimMap: RequestedSelfAttestedClaimMap = mutableMapOf()

    fun addRequestedIdToken(idTokenAttestation: IdTokenAttestation, rawToken: String) {
        requestedIdTokenMap[idTokenAttestation.configuration] = rawToken
    }

    fun addRequestedSelfAttestedClaim(field: String, claim: String) {
        requestedSelfAttestedClaimMap[field] = claim
    }

    fun addRequestedVch(presentationAttestation: PresentationAttestation, vch: VerifiableCredentialHolder) {
        requestedVchMap[presentationAttestation] = vch
    }

    fun getRequestedIdTokens(): RequestedIdTokenMap {
        return requestedIdTokenMap
    }

    fun getRequestedSelfAttestedClaims(): RequestedSelfAttestedClaimMap {
        return requestedSelfAttestedClaimMap
    }

    fun getRequestedVchs(): RequestedVchMap {
        return requestedVchMap
    }
}

class PresentationResponse(override val request: PresentationRequest, override val responder: Identifier) : Response(request, request.content.redirectUrl, responder) {
    private val requestedVchPresentationSubmissionMap: RequestedVchPresentationSubmissionMap = mutableMapOf()
    fun addRequestedVchClaims(
        credentialPresentationInputDescriptor: CredentialPresentationInputDescriptor,
        vch: VerifiableCredentialHolder
    ) {
        requestedVchPresentationSubmissionMap[credentialPresentationInputDescriptor] = vch
    }

    fun getRequestedVchClaims(): RequestedVchPresentationSubmissionMap {
        return requestedVchPresentationSubmissionMap
    }

    fun createReceiptsForPresentedVerifiableCredentials(entityDid: String, entityName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        requestedVchPresentationSubmissionMap.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().cardId, entityDid, entityName)
            receiptList.add(receipt)
        }
        return receiptList
    }

    private fun createReceipt(action: ReceiptAction, vcId: String, entityDid: String, entityName: String): Receipt {
        val date = System.currentTimeMillis()
        return Receipt(
            action = action,
            vcId = vcId,
            activityDate = date,
            entityIdentifier = entityDid,
            entityName = entityName
        )
    }
}

typealias RequestedIdTokenMap = MutableMap<String, String>
typealias RequestedSelfAttestedClaimMap = MutableMap<String, String>
typealias RequestedVchMap = MutableMap<PresentationAttestation, VerifiableCredentialHolder>
typealias RequestedVchPresentationSubmissionMap = MutableMap<CredentialPresentationInputDescriptor, VerifiableCredentialHolder>
