package com.microsoft.useragentSdk.crypto.models.webCryptoApi

/**
 * @see https://www.w3.org/TR/WebCryptoAPI/#dfn-AesGcmParams
 */
class AesGcmParams(name: String, val iv: ByteArray, val additionalData: ByteArray, val tagLength: Byte,
                   additionalParams: Map<String, Any> = emptyMap()): Algorithm(name, additionalParams) {
    // iv may be up to 2^64-1 bytes long.
    // tagLength must be enforced between 0 and 128
}