package com.microsoft.useragentSdk.crypto.models.webCryptoApi

class EcdsaParams(name: String, hash: Algorithm, additionalParams: Map<String, Any>): Algorithm(name,
    additionalParams + mapOf("hash" to hash)) {

    constructor(name: String, hash: Algorithm): this(name, hash, emptyMap())

    val hash: Algorithm
        get() = get("hash") as Algorithm
}