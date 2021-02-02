// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.linkedDomains

import com.microsoft.did.sdk.util.Constants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DomainLinkageCredentialContent(
    @SerialName(Constants.CONTEXT)
    val context: List<String>,
    val issuer: String,
    val issuanceDate: String,
    val expirationDate: String,
    val type: List<String>,
    val credentialSubject: DomainLinkageCredentialSubject
)