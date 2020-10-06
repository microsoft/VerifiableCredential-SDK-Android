/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.models.VerifiableCredential
import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.util.serializer.Serializer

fun formVerifiableCredential(rawToken: String, vcId: String? = null, serializer: Serializer): VerifiableCredential {
    val vcContents = unwrapSignedVerifiableCredential(rawToken, serializer)
    return VerifiableCredential(vcContents.jti, rawToken, vcContents, vcId ?: vcContents.jti)
}

fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String, serializer: Serializer): VerifiableCredentialContent {
    val token = JwsToken.deserialize(signedVerifiableCredential, serializer)
    return serializer.parse(VerifiableCredentialContent.serializer(), token.content())
}