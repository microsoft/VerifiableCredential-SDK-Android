// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.models.identifierdocument

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentifierMetadata(
    @SerialName("operationPublicKeys")
    val operationPublicKeys: List<IdentifierDocumentPublicKey>? = null,
    val recoveryKey: JsonWebKey? = null
)