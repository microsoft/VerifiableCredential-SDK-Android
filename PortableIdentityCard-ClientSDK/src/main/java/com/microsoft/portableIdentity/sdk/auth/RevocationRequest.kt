// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth

import com.microsoft.portableIdentity.sdk.cards.PortableIdentityCard

class RevocationRequest(override val audience: String, val card: PortableIdentityCard, val reason: String): ActionRequest {

    private val relyingParties: MutableList<String> = mutableListOf()

    fun getRelyingParties(): List<String> {
        return relyingParties
    }

    fun addRelyingParty(identifier: String) {
        relyingParties.add(identifier)
    }
}