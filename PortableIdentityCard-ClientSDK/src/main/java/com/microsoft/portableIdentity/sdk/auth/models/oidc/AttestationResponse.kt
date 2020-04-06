// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.models.oidc

import kotlinx.serialization.Serializable

@Serializable
data class AttestationResponse(
        val selfIssued: Map<String, String>? = null,

        val idTokens: Map<String, String>? = null,

        val presentations: Map<String, String>? = null
)