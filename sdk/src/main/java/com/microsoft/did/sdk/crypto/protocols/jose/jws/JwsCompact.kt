package com.microsoft.did.sdk.crypto.protocols.jose.jws

import kotlinx.serialization.Serializable

/**
 * JWS compact format.
 */
@Serializable
data class JwsCompact(
    /**
     * The application-specific payload.
     */
    val payload: String,
    /**
     * The protected (signed) header.
     */
    val protected: String,

    /**
     * The JWS Signature
     */
    val signature: String
)