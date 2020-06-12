// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk.identifier.models.payload.document

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Class for defining public key used for encryption/signing inside document payload sent to Sidetree operation
 */

@Serializable
data class IdentifierDocumentPublicKeyInput(
    /**
     * The id of the public key in the format
     * {keyIdentifier}
     */
    val id: String,

    /**
     * The type of the public key.
     */
    val type: String,

    /**
     * The owner of the key.
     */
    val controller: String? = null,

    /**
     * The JWK public key.
     */
    val jwk: JsonWebKey,

    @SerialName("purpose")
    val purpose: List<String>
)