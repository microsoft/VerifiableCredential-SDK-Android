package com.microsoft.useragentSdk.crypto.models.webCryptoApi

/**
 * @see https://www.w3.org/TR/WebCryptoAPI/#dfn-AesGcmParams
 */
class AesGcmParams(name: String, iv: ByteArray, additionalData: ByteArray, tagLength: Byte,
                   additionalParams: Map<String, Any>): Algorithm(name, additionalParams + mapOf(
    "iv" to iv, "additionalData" to additionalData, "tagLength" to tagLength
)) {
    // iv may be up to 2^64-1 bytes long.
    // tagLength must be enforced between 0 and 128
    constructor(name: String, iv: ByteArray, additionalData: ByteArray, tagLength: Byte): this(name, iv, additionalData, tagLength, emptyMap()) {}
}