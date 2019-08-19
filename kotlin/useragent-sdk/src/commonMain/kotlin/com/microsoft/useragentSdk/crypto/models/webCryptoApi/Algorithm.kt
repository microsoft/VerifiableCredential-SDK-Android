package com.microsoft.useragentSdk.crypto.models.webCryptoApi

open class Algorithm(name: String, additionalParams: Map<String, Any>):
    Map<String, Any> by HashMap<String, Any>(original = additionalParams + mapOf("name" to name)) {

    constructor(name: String) : this(name, emptyMap()) {
    }

    val name: String
        get() = get("name") as String
}