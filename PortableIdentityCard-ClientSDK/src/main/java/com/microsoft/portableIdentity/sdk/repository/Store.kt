// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.repository

import com.microsoft.portableIdentity.sdk.credentials.ClaimObject

interface Store {
    suspend fun saveClaim(claim: ClaimObject): Boolean

    suspend fun getClaims(): List<ClaimObject>
}