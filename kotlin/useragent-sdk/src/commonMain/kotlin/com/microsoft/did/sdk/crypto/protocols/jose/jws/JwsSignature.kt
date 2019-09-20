package com.microsoft.did.sdk.crypto.protocols.jose.jws

import kotlinx.serialization.Serializable

/**
 * JWS signature used by the general JSON
 */
@Serializable
data class JwsSignature (
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