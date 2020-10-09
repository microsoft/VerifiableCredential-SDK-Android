// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.serviceResponses

import com.microsoft.did.sdk.credential.service.models.dnsBinding.DomainLinkageCredential
import com.microsoft.did.sdk.util.Constants.CONTEXT
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DnsBindingResponse(
    @SerialName(CONTEXT)
    val context: String,
    val linked_dids: List<DomainLinkageCredential>
): ServiceResponse