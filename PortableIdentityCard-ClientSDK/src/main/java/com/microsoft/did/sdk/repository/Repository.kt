// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.repository

import com.microsoft.did.sdk.credentials.ClaimObject

class Repository(private val store: Store) {

    suspend fun saveClaim(claim: ClaimObject): Boolean {
        return store.saveClaim(claim)
    }

    suspend fun getClaims(): List<ClaimObject> {
        return store.getClaims()
    }
}