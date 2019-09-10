package com.microsoft.did.sdk.identifier

import com.microsoft.did.sdk.crypto.keys.PublicKey
import kotlinx.serialization.Serializable

/**
 * Data Class for defining an Identifier Document
 * Public Key.
 */
@Serializable
data class IdentifierDocumentPublicKey (
    /**
     * The id of the public key in the format
     * #{keyIdentifier}.
     */
    val id: String,

    /**
     * The type of the public key.
     */
    val type: String,

    /**
     * The owner of the key.
     */
    val owner: String?,

    /**
     * The JWK public key.
     */
    val publicKeyJwk: PublicKey
    )