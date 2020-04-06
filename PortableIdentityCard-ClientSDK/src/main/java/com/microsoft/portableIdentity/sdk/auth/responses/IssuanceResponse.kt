// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.responses

import com.microsoft.portableIdentity.sdk.auth.models.attestationBindings.IdTokenBinding
import com.microsoft.portableIdentity.sdk.auth.models.attestationBindings.SelfIssuedBinding
import com.microsoft.portableIdentity.sdk.auth.models.attestations.IdTokenAttestation
import com.microsoft.portableIdentity.sdk.auth.requests.IssuanceRequest

class IssuanceResponse(val request: IssuanceRequest): OidcResponse(request.contract.input.credentialIssuer) {

    val contractUrl: String = request.contract.display.contract

    private val collectedTokens: MutableList<IdTokenBinding> = mutableListOf()

    private val collectedSelfIssued: MutableList<SelfIssuedBinding> = mutableListOf()

    fun addIdToken(idToken: String, attestation: IdTokenAttestation) {
        val tokenBinding = IdTokenBinding(idToken, attestation)
        collectedTokens.add(tokenBinding)
    }

    fun addSelfIssuedClaim(claim: String, attestation: String) {
        val selfIssuedBinding = SelfIssuedBinding(claim, attestation)
        collectedSelfIssued.add(selfIssuedBinding)
    }

    fun getIdTokenBindings(): List<IdTokenBinding> {
        return collectedTokens
    }

    fun getSelfIssuedClaimBindings(): List<SelfIssuedBinding> {
        return collectedSelfIssued
    }
}