// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.credential.models

import kotlinx.serialization.Serializable

@Serializable
data class RevocationReceipt(
    val jti: String,
    val iss: String,
    val credentialStatus: CredentialStatus,
    val iat: Long
) {
    val rp: Array<String>? = null
}