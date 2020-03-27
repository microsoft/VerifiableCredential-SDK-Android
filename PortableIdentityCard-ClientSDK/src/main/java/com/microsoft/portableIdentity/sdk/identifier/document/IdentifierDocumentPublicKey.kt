package com.microsoft.portableIdentity.sdk.identifier.document

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
    val controller: String? = null,

    @Deprecated("against spec", ReplaceWith("this.controller"))
    val owner: String? = null,

    /**
     * The HEX public key.
     */
    val publicKeyHex: String
) {
}
