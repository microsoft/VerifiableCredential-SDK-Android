/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.cards.Card
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcRequestContent
import com.microsoft.portableIdentity.sdk.auth.models.oidc.OidcResponseContent
import com.microsoft.portableIdentity.sdk.auth.requests.OidcRequest

/**
 * OIDC Response formed from a Request.
 *
 * @param request that response is responding to.
 * @param signer optional parameter used to protect the response.
 */
open abstract class OidcResponse(override val audience: String): Response {

    /**
     * list of collected credentials to be sent in response.
     */
    private val collectedCredentials: MutableList<Card> = mutableListOf()

    /**
     * Add Credential to be put into response.
     *
     * @param credential to be added to response.
     */
    override fun addCredential(credential: Card) {
        collectedCredentials.add(credential)
    }

    /**
     * Get Credentials
     */
    fun getCredentials(): MutableList<Card> {
        return collectedCredentials
    }
}