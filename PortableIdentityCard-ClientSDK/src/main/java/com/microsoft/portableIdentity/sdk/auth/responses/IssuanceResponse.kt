/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.requests.CredentialRequest

class IssuanceResponse(val request: CredentialRequest.IssuanceRequest): OidcResponse(request.contract.input.credentialIssuer) {

    val contractUrl: String = request.contractUrl

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
}