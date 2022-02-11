// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.service.models.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RevocationResponseClaims(
    @SerialName("rp")
    val rpList: List<String> = emptyList(),

    val reason: String = "",

    val vc: String = "",

    var did: String = ""
) : OidcResponseClaims()