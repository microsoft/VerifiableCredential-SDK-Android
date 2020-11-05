/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import kotlinx.serialization.json.Json

fun formVerifiableCredential(rawToken: String, serializer: Json): VerifiableCredential {
    val vcContents = unwrapSignedVerifiableCredential(rawToken, serializer)
    return VerifiableCredential(vcContents.jti, rawToken, vcContents)
}

fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String, serializer: Json): VerifiableCredentialContent {
    val token = JwsToken.deserialize(signedVerifiableCredential, serializer)
    return serializer.decodeFromString(VerifiableCredentialContent.serializer(), token.content())
}