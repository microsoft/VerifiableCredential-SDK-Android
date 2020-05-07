/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.auth.requests.Request
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction

/**
 * OIDC Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(open val request: Request, override val audience: String): ServiceRequest {

    private val collectedCards: MutableMap<String, PortableIdentityCard> = mutableMapOf()

    private val collectedTokens: MutableMap<String, String> = mutableMapOf()

    private val collectedSelfIssued: MutableMap<String, String> = mutableMapOf()

    fun addIdToken(configuration: String, token: String) {
        collectedTokens[configuration] = token
    }

    fun addSelfIssuedClaim(field: String, claim: String) {
        collectedSelfIssued[field] = claim
    }

    fun addCard(card: PortableIdentityCard, type: String) {
        collectedCards[type] = card
    }

    fun getCollectedIdTokens(): Map<String, String> {
        return collectedTokens
    }

    fun getCollectedSelfIssuedClaims(): Map<String, String> {
        return collectedSelfIssued
    }

    fun getCollectedCards(): Map<String, PortableIdentityCard> {
        return collectedCards
    }

    fun transformCollectedCards(transform: (input: PortableIdentityCard) -> PortableIdentityCard) {
        collectedCards.mapValues {
            transform(it.value)
        }
    }

    fun createReceiptsForPresentedCredentials(requestToken: String, entityDid: String, entityHostName: String, entityName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        collectedCards.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().cardId, entityDid, entityHostName, entityName, requestToken)
            receiptList.add(receipt)
        }
        return receiptList
    }

    fun createReceipt(action: ReceiptAction, cardId: String, entityDid: String, entityHostName: String, entityName: String, requestToken: String): Receipt {
        val date = System.currentTimeMillis()
        return Receipt(action = action,
            cardId = cardId,
            activityDate = date,
            entityIdentifier = entityDid,
            entityHostName = entityHostName,
            entityName = entityName,
            token = requestToken)
    }
}

class IssuanceResponse(override val request: IssuanceRequest): Response(request, request.contract.input.credentialIssuer)
class PresentationResponse(override val request: PresentationRequest): Response(request, request.content.redirectUrl)