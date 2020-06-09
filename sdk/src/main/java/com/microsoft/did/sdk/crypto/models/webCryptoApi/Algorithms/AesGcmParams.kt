package com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms

import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants

/**
 * @see https://www.w3.org/TR/WebCryptoAPI/#dfn-AesGcmParams
 */
class AesGcmParams(
    val iv: ByteArray, val additionalData: ByteArray, val tagLength: Byte,
    additionalParams: Map<String, Any> = emptyMap()
) : Algorithm(W3cCryptoApiConstants.AesGcm.value, additionalParams) {
    // iv may be up to 2^64-1 bytes long.
    // tagLength must be enforced between 0 and 128
}