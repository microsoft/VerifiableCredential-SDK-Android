// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service

import com.microsoft.did.sdk.credential.models.PortableIdentityCard
import com.microsoft.did.sdk.credential.receipts.Receipt
import com.microsoft.did.sdk.credential.receipts.ReceiptAction
import com.microsoft.did.sdk.credential.service.models.attestations.PresentationAttestation
import com.microsoft.did.sdk.credential.service.models.holders.RequestedPresentationPicHolder

/**
 * OIDC Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, val audience: String) {

    private val collectedCards: MutableMap<String, RequestedPresentationPicHolder> = mutableMapOf()

    private val collectedTokens: MutableMap<String, String> = mutableMapOf()

    // EXPERIMENTAL
    private val collectedSelfIssued: MutableMap<String, String> = mutableMapOf()

    fun addIdToken(configuration: String, token: String) {
        collectedTokens[configuration] = token
    }

    fun addSelfIssuedClaim(field: String, claim: String) {
        collectedSelfIssued[field] = claim
    }

    fun addCard(card: PortableIdentityCard, presentationAttestation: PresentationAttestation) {
        collectedCards[presentationAttestation.credentialType] = RequestedPresentationPicHolder(card, presentationAttestation)
    }

    fun getCollectedIdTokens(): Map<String, String>? {
        if (collectedTokens.isEmpty()) {
            return null
        }
        return collectedTokens
    }

    fun getCollectedSelfIssuedClaims(): Map<String, String>? {
        if (collectedSelfIssued.isEmpty()) {
            return null
        }
        return collectedSelfIssued
    }

    fun getCollectedCards(): Map<String, RequestedPresentationPicHolder>? {
        if (collectedCards.isEmpty()) {
            return null
        }
        return collectedCards
    }

    fun createReceiptsForPresentedCredentials(entityDid: String, entityName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        collectedCards.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().portableIdentityCard.cardId, entityDid, entityName)
            receiptList.add(receipt)
        }
        return receiptList
    }

    private fun createReceipt(action: ReceiptAction, cardId: String, entityDid: String, entityName: String): Receipt {
        val date = System.currentTimeMillis()
        return Receipt(
            action = action,
            cardId = cardId,
            activityDate = date,
            entityIdentifier = entityDid,
            entityName = entityName
        )
    }
}

class IssuanceResponse(override val request: IssuanceRequest) : Response(request, request.contract.input.credentialIssuer)
class PresentationResponse(override val request: PresentationRequest) : Response(request, request.content.redirectUrl)