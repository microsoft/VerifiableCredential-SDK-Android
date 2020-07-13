/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.requestMappings.VerifiableCredentialHolderRequestMapping
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction

/**
 * Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, val audience: String) {
    
    private val verifiableCredentialHolderRequestMappings: MutableList<VerifiableCredentialHolderRequestMapping> = mutableListOf()

    private val idTokenContexts: MutableMap<String, String> = mutableMapOf()

    // EXPERIMENTAL
    private val selfAttestedClaimContexts: MutableMap<String, String> = mutableMapOf()

    fun addIdTokenContext(idTokenAttestation: IdTokenAttestation, token: String) {
        idTokenContexts[idTokenAttestation.configuration] = token
    }

    fun addSelfAttestedClaimContext(field: String, claim: String) {
        selfAttestedClaimContexts[field] = claim
    }

    fun addVerifiablePresentationContext(card: VerifiableCredentialHolder, presentationAttestation: PresentationAttestation) {
        verifiableCredentialHolderRequestMappings.add(VerifiableCredentialHolderRequestMapping(card, presentationAttestation))
    }

    fun getIdTokenContexts(): MutableMap<String, String> {
        return idTokenContexts
    }

    fun getSelfAttestedClaimContexts(): MutableMap<String, String> {
        return selfAttestedClaimContexts
    }

    fun getVerifiablePresentationContexts(): List<VerifiableCredentialHolderRequestMapping> {
        return verifiableCredentialHolderRequestMappings
    }

    fun createReceiptsForPresentedVerifiableCredentials(entityDid: String, entityName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        verifiableCredentialHolderRequestMappings.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.verifiablePresentationHolder.cardId, entityDid, entityName)
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

class IssuanceResponse(override val request: IssuanceRequest) : Response(request, request.contract.input.credentialIssuer)
class PresentationResponse(override val request: PresentationRequest) : Response(request, request.content.redirectUrl)