package com.microsoft.portableIdentity.sdk.crypto.keys.rsa

import com.microsoft.portableIdentity.sdk.crypto.keys.KeyType
import com.microsoft.portableIdentity.sdk.crypto.keys.PublicKey
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.utilities.SdkLog

class RsaPublicKey(jwk: JsonWebKey): PublicKey(jwk) {
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