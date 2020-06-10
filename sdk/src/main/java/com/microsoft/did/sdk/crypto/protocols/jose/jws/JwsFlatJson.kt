package com.microsoft.did.sdk.crypto.protocols.jose.jws

import kotlinx.serialization.Serializable

/**
 * JWS flattened JSON format.
 */
@Serializable
data class JwsFlatJson(
    /**
     * The application-specific payload.
     */
    val payload: String,
    /**
     * The protected (signed) header.
     */
    val protected: String,

    /**
     * The unprotected (unverified) header.
     */
    val header: Map<String, String>?,

    /**
     * The JWS Signature
     */
    val signature: String
)