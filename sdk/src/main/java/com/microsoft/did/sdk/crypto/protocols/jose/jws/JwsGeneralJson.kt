package com.microsoft.did.sdk.crypto.protocols.jose.jws

import kotlinx.serialization.Serializable

/**
 * JWS general JSON format.
 */
@Serializable
data class JwsGeneralJson(
    /**
     * The application-specific non-encoded payload.
     */
    val payload: String,

    /**
     * The Signatures
     */
    val signatures: List<JwsSignature>
)