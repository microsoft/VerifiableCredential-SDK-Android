package com.microsoft.did.sdk.crypto.protocols.jose.jws

import kotlinx.serialization.Serializable

/**
 * JWS flattened JSON format.
 */
@Serializable
data class JwsFlatJson (
    /**
     * The application-specific payload.
     */
    val payload: String,
    /**
     * The protected (signed) header.
     */
    val protected: MutableMap<String, String>?,

    /**
     * The unprotected (unverified) header.
     */
    val header: MutableMap<String, String>?,

    /**
     * The JWS Signature
     */
    val signature: String
)