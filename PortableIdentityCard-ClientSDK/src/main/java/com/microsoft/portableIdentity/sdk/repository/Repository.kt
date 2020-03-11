// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository

import com.microsoft.portableIdentity.sdk.credentials.ClaimObject

class Repository(private val store: Store) {

    suspend fun saveClaim(claim: ClaimObject): Boolean {
        return store.saveClaim(claim)
    }

    suspend fun getClaims(): List<ClaimObject> {
        return store.getClaims()
    }
}