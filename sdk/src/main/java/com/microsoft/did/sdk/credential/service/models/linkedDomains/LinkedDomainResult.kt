// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.linkedDomains

import kotlinx.serialization.Serializable

@Serializable
sealed class LinkedDomainResult<out S> {
    class Verified<out S>(val payload: S) : LinkedDomainResult<S>()
    class UnVerified<out S>(val payload: S) : LinkedDomainResult<S>()
}