// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.identifier.models.identifierdocument

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentifierMetadata(
    @SerialName("operationPublicKeys")
    val operationPublicKeys: List<IdentifierDocumentPublicKey>,
    val recoveryKey: JsonWebKey
)