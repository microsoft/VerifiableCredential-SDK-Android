// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.dnsBinding

import kotlinx.serialization.Serializable

@Serializable
data class DomainLinkageCredentialSubject (
    val id: String,
    val origin: String
)