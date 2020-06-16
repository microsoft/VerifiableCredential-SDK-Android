// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service

<<<<<<< HEAD
import com.microsoft.did.sdk.credential.models.PortableIdentityCard
import com.microsoft.did.sdk.credential.receipts.Receipt
import com.microsoft.did.sdk.credential.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.attestations.IdTokenAttestation
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.contexts.IdTokenContext
import com.microsoft.did.sdk.credential.service.models.contexts.VerifiablePresentationContext
import com.microsoft.did.sdk.credential.service.models.contexts.SelfAttestedClaimContext
=======
import com.microsoft.did.sdk.credential.models.VerifiableCredentialHolder
import com.microsoft.did.sdk.credential.models.receipts.Receipt
import com.microsoft.did.sdk.credential.models.receipts.ReceiptAction
>>>>>>> master

/**
 * Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, val audience: String) {

<<<<<<< HEAD
    private val verifiablePresentationContexts: MutableMap<String, VerifiablePresentationContext> = mutableMapOf()
=======
    private val collectedVchs: MutableMap<String, VerifiableCredentialHolder> = mutableMapOf()
>>>>>>> master

    private val idTokenContexts: MutableMap<String, IdTokenContext> = mutableMapOf()

    // EXPERIMENTAL
    private val selfAttestedClaimContexts: MutableMap<String, SelfAttestedClaimContext> = mutableMapOf()

    fun addIdTokenContext(idTokenAttestation: IdTokenAttestation, token: String) {
        idTokenContexts[idTokenAttestation.configuration] = IdTokenContext(idTokenAttestation, token)
    }

    fun addSelfAttestedClaimContext(field: String, claim: String) {
        selfAttestedClaimContexts[field] = SelfAttestedClaimContext(field, claim)
    }

<<<<<<< HEAD
    fun addVerifiablePresentationContext(card: PortableIdentityCard, presentationAttestation: PresentationAttestation) {
        verifiablePresentationContexts[presentationAttestation.credentialType] = VerifiablePresentationContext(card, presentationAttestation)
=======
    fun addVerifiableCredential(vch: VerifiableCredentialHolder, type: String) {
        collectedVchs[type] = vch
>>>>>>> master
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

<<<<<<< HEAD
    fun getVerifiablePresentationContexts(): Map<String, VerifiablePresentationContext>? {
        if (verifiablePresentationContexts.isEmpty()) {
            return null
        }
        return verifiablePresentationContexts
=======
    fun getCollectedVchs(): Map<String, VerifiableCredentialHolder>? {
        if (collectedVchs.isEmpty()) {
            return null
        }
        return collectedVchs
>>>>>>> master
    }

    fun createReceiptsForPresentedVerifiableCredentials(entityDid: String, entityName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
<<<<<<< HEAD
        verifiablePresentationContexts.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().portableIdentityCard.cardId, entityDid, entityName)
=======
        collectedVchs.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().cardId, entityDid, entityName)
>>>>>>> master
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