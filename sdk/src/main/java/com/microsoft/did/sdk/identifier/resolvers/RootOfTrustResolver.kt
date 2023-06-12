// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.resolvers

import com.microsoft.did.sdk.credential.service.models.linkedDomains.LinkedDomainResult

interface RootOfTrustResolver {
    suspend fun resolve(did: String): LinkedDomainResult
}