/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.credential.models

import com.microsoft.did.sdk.util.Constants.RELYING_PARTY_LIST
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
    @SerialName(RELYING_PARTY_LIST)
    val relyingPartyList: Array<String>? = null
}