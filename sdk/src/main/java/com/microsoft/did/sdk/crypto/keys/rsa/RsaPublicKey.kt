package com.microsoft.did.sdk.crypto.keys.rsa

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

class RsaPublicKey(jwk: JsonWebKey) : PublicKey(jwk) {
    override fun minimumAlphabeticJwk(): String {
        return "{\"e\":\"${e}\",\"kty\":\"${kty.value}\",\"n\":\"${n}\"}"
    }

    override var kty = KeyType.RSA

    val n = key.n
    val e = key.e

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = kty.value,
            alg = alg,
            use = use?.value,
            key_ops = key_ops?.map { use -> use.value },
            kid = kid,
            e = e,
            n = n
        )
    }
}