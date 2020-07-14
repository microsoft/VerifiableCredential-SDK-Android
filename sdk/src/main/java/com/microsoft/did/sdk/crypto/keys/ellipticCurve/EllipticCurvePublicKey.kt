package com.microsoft.did.sdk.crypto.keys.ellipticCurve

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage

class EllipticCurvePublicKey(key: JsonWebKey) : PublicKey(key) {
    override fun minimumAlphabeticJwk(): String {
        return "{\"crv\":\"$crv\",\"kty\":\"${kty.value}\",\"x\":\"$x\",\"y\":\"$y\"}"
    }

    var crv = key.crv
    var x = key.x
    var y = key.y
    override var kty = KeyType.EllipticCurve
    override var key_ops: List<KeyUsage>? = listOf(KeyUsage.Verify)

    override fun toJWK(): JsonWebKey {
        return JsonWebKey(
            kty = kty.value,
            alg = alg,
            kid = kid,
            key_ops = key_ops?.map { use -> use.value },
            use = use?.value,
            crv = crv,
            x = x,
            y = y
        )
    }
}