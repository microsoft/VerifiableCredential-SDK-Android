package com.microsoft.useragentSdk.crypto.models.webCryptoApi

class RsaOaepParams(name: String, label: ByteArray?, additionalParams: Map<String, Any>): Algorithm(name,
    if (label != null) { additionalParams + mapOf("label" to label) } else { additionalParams } ) {

    constructor(name: String, label: ByteArray): this(name, label, emptyMap()) {}
    constructor(name: String, additionalParams: Map<String, Any>): this(name = name,
        label = null, additionalParams = additionalParams) {}
    constructor(name: String): this(name = name, label = null, additionalParams = emptyMap())
}