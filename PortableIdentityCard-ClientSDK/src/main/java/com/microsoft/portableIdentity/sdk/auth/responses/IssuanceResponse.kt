// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.models.attestationBindings.IdTokenBinding
import com.microsoft.portableIdentity.sdk.auth.models.attestationBindings.SelfIssuedBinding
import com.microsoft.portableIdentity.sdk.auth.models.attestations.IdTokenAttestation
import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest

class IssuanceResponse(val request: IssuanceRequest): OidcResponse(request.contract.input.credentialIssuer) {

    val contractUrl: String = request.contract.display.contract

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