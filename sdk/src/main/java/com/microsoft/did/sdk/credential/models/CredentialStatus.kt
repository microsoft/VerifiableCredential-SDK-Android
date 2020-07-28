// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.models

import kotlinx.serialization.Serializable

@Serializable
data class CredentialStatus(val id: String, val status: String) {
    var reason: String? = null
}