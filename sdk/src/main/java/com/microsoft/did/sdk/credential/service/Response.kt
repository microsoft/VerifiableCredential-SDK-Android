// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.contexts.IdTokenContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiablePresentationContext
import com.microsoft.did.sdk.credential.service.models.contexts.SelfAttestedClaimContext
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction

/**
 * Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, val audience: String) {
    
    private val verifiablePresentationContexts: MutableMap<String, VerifiablePresentationContext> = mutableMapOf()

    private val idTokenContexts: MutableMap<String, IdTokenContext> = mutableMapOf()

    // EXPERIMENTAL
    private val selfAttestedClaimContexts: MutableMap<String, SelfAttestedClaimContext> = mutableMapOf()

    fun addIdTokenContext(idTokenAttestation: IdTokenAttestation, token: String) {
        idTokenContexts[idTokenAttestation.configuration] = IdTokenContext(idTokenAttestation, token)
    }

    fun addSelfAttestedClaimContext(field: String, claim: String) {
        selfAttestedClaimContexts[field] = SelfAttestedClaimContext(field, claim)
    }

    fun addVerifiablePresentationContext(card: VerifiableCredentialHolder, presentationAttestation: PresentationAttestation) {
        verifiablePresentationContexts[presentationAttestation.credentialType] = VerifiablePresentationContext(card, presentationAttestation)
    }

    fun getIdTokenContexts(): Map<String, IdTokenContext>? {
        if (idTokenContexts.isEmpty()) {
            return null
        }
        return idTokenContexts
    }

    fun getSelfAttestedClaimContexts(): Map<String, SelfAttestedClaimContext>? {
        if (selfAttestedClaimContexts.isEmpty()) {
            return null
        }
        return selfAttestedClaimContexts
    }

    fun getVerifiablePresentationContexts(): Map<String, VerifiablePresentationContext>? {
        if (verifiablePresentationContexts.isEmpty()) {
            return null
        }
        return verifiablePresentationContexts
    }

    fun createReceiptsForPresentedVerifiableCredentials(entityDid: String, entityName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        verifiablePresentationContexts.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().verifiablePresentationHolder.primeVcId, entityDid, entityName)
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