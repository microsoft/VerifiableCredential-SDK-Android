package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

/**
 * Password-Based Key Derivation v2, described in @see https://tools.ietf.org/html/rfc2898
 * @see https://www.w3.org/TR/WebCryptoAPI/#pbkdf2
 */
class Pbkdf2Params (
    val salt: ByteArray,
    val iterations: ULong,
    val hash: Algorithm,
    additionalParams: Map<String, Any> = emptyMap()
): Algorithm(W3cCryptoApiConstants.Pbkdf2.value, additionalParams) {
}