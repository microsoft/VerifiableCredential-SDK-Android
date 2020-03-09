// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.auth.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdToken(
    val iss: String,
    val sub: String,
    val aud: String,
    val exp: Int,
    val iat: Int,
    @SerialName("auth_time")
    val authTime: Int? = null,
    val nonce: String,
    @SerialName("acr")
    val authenticationContextClass: String? = null,
    @SerialName("amr")
    val authenticationMethods: List<String>? = null,
    @SerialName("azp")
    val authorizedParty: String? = null
)