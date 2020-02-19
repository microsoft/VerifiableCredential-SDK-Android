// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.repository

import com.microsoft.did.sdk.credentials.ClaimObject

interface Store {
    suspend fun saveClaim(claim: ClaimObject): Boolean

    suspend fun getClaims(): List<ClaimObject>
}