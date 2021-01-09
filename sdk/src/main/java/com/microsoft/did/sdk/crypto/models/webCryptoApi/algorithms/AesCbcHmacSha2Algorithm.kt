package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

/**
 * Algorithm retrofit for AES in CBC mode with HMAC using SHA2
 * @see https://tools.ietf.org/html/rfc7518#section-5.2.2
 */
class AesCbcHmacSha2Algorithm (
    name: String,
    val iv: ByteArray,
    val aad: ByteArray,
    val tag: ByteArray? = null,
    additionalParams: Map<String, Any> = emptyMap()
): Algorithm(name, additionalParams) {
}