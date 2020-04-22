/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

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
abstract class OidcResponse(override val audience: String): Response {

    private val collectedCards: MutableMap<String, PortableIdentityCard> = mutableMapOf()

    override fun addCard(card: PortableIdentityCard, type: String) {
        collectedCards[type] = card
    }

    fun getCardBindings(): Map<String, PortableIdentityCard> {
        return collectedCards
    }

    fun createReceiptsForPresentedCards(action: ReceiptAction, requestToken: String): List<Receipt> {
        val receiptList = mutableListOf<Receipt>()
        collectedCards.forEach {
            val receipt = createReceipt(action, it.component2().id, it.component2().verifiableCredential.contents.iss, requestToken)
            receiptList.add(receipt)
        }
        return receiptList
    }

    private fun createReceipt(action: ReceiptAction, cardId: String, relyingPartyDid: String, requestToken: String): Receipt {
        val date = Date().time / Constants.MILLISECONDS_IN_A_SECOND
        return Receipt(action = action,
            cardId = cardId,
            activityDate = date,
            entity = relyingPartyDid,
            token = requestToken)
    }
}