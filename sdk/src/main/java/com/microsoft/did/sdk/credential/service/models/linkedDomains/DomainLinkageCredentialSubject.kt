// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.linkedDomains

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DomainLinkageCredentialSubject (
    @SerialName("id")
    val did: String,
    @SerialName("origin")
    val domainUrl: String
)