// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import com.microsoft.did.sdk.cards.verifiableCredential.VerifiableCredentialContent
import com.microsoft.did.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.did.sdk.utilities.Serializer

fun unwrapSignedVerifiableCredential(signedVerifiableCredential: String, serializer: Serializer): VerifiableCredentialContent {
    val token = JwsToken.deserialize(signedVerifiableCredential, serializer)
    return serializer.parse(VerifiableCredentialContent.serializer(), token.content())
}