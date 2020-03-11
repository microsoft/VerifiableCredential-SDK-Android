// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository

import com.microsoft.portableIdentity.sdk.credentials.ClaimObject

class InMemoryStore : Store {

    private val claimList: MutableList<ClaimObject> = ArrayList()

    override suspend fun saveClaim(claim: ClaimObject): Boolean {
        claimList.add(claim)
        return true
    }

    override suspend fun getClaims(): List<ClaimObject> {
        return claimList
    }
}