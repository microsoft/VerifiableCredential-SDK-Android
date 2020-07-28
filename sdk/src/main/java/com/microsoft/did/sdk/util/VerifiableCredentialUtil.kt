/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package com.microsoft.did.sdk.util

import com.microsoft.did.sdk.credential.models.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.util.serializer.Serializer

fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String, serializer: Serializer): VerifiableCredentialContent {
    val token = JwsToken.deserialize(signedVerifiableCredential, serializer)
    return serializer.parse(VerifiableCredentialContent.serializer(), token.content())
}