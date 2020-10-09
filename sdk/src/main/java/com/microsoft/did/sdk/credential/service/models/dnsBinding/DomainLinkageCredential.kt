// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.dnsBinding

import kotlinx.serialization.Serializable

@Serializable
data class DomainLinkageCredential (
    val sub: String,
    val iss: String,
    val nbf: Long,
    val vc: DomainLinkageCredentialContent
)