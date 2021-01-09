package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

class HmacKeyGenParams(
    // The inner hash function to use.
    val hash: Algorithm,
    // The length (in bits) of the key.
    val length: ULong,
    additionalParams: Map<String, Any> = emptyMap()
): Algorithm(W3cCryptoApiConstants.AesCbc.value, additionalParams) {
}