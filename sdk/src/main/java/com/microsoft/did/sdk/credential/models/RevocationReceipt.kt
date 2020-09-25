/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RevocationReceipt(
    @SerialName("jti")
    val jwtId: String,
    @SerialName("iss")
    val issuer: String,
    val credentialStatus: CredentialStatus,
    @SerialName("iat")
    val issuedTime: Long
) {
    @SerialName("rp")
    val relyingPartyList: Array<String>? = null
}