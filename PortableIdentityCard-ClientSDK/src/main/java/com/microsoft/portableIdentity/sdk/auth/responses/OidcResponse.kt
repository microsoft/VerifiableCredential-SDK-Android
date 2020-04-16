/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard

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
}