package com.microsoft.did.sdk.crypto.keys.rsa

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

class RsaPublicKey(jwk: JsonWebKey): PublicKey(jwk) {
    override fun minimumAlphabeticJwk(): String {
        return "{\"e\":\"${e}\",\"kty\":\"${kty.value}\",\"n\":\"${n}\"}"
    }

    override var kty = KeyType.RSA

    val n = key.n ?: throw Error("Json Web key parameter \"n\" is required.")
    val e = key.e ?: throw Error("Json Web key parameter \"e\" is required.")
}