/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest
import com.microsoft.portableIdentity.sdk.auth.requests.PresentationRequest
import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard
import com.microsoft.portableIdentity.sdk.cards.receipts.Receipt
import com.microsoft.portableIdentity.sdk.cards.receipts.ReceiptAction
import com.microsoft.portableIdentity.sdk.utilities.Constants
import java.util.*

/**
 * OIDC Response formed from a Request.
 *
 * @param audience entity to send the response to.
 */
sealed class Response(val audience: String) {

    private val collectedCards: MutableMap<String, PortableIdentityCard> = mutableMapOf()

    private val collectedTokens: MutableMap<String, String> = mutableMapOf()

    private val collectedSelfIssued: MutableMap<String, String> = mutableMapOf()

    fun addIdToken(configuration: String, token: String) {
        collectedTokens[configuration] = token
    }

    fun addSelfIssuedClaim(field: String, claim: String) {
        collectedSelfIssued[field] = claim
    }

    fun getIdTokenBindings(): Map<String, String> {
        return collectedTokens
    }

    fun getSelfIssuedClaimBindings(): Map<String, String> {
        return collectedSelfIssued
    }

    fun addCard(card: PortableIdentityCard, type: String) {
        collectedCards[type] = card
    }

    fun getCardBindings(): Map<String, PortableIdentityCard> {
        return collectedCards
    }

    fun createReceiptsForPresentedCredentials(requestToken: String, entityDid: String, entityHostName: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        collectedCards.forEach {
            val receipt = createReceipt(ReceiptAction.Presentation, it.component2().id, entityDid, entityHostName, requestToken)
            receiptList.add(receipt)
        }
        return receiptList
    }

    fun createReceipt(action: ReceiptAction, cardId: String, entityDid: String, entityHostName: String, requestToken: String): Receipt {
        val date = Date().time / Constants.MILLISECONDS_IN_A_SECOND
        return Receipt(action = ReceiptAction.Presentation,
            cardId = cardId,
            activityDate = date,
            entityIdentifier = entityDid,
            entityHostName = entityHostName,
            token = requestToken)
    }
}

class IssuanceResponse(val request: IssuanceRequest): Response(request.contract.input.credentialIssuer)
class PresentationResponse(val request: PresentationRequest): Response(request.content.redirectUrl)