package com.microsoft.useragentSdk.crypto.keys.ellipticCurve

import com.microsoft.useragentSdk.crypto.keys.KeyType
import com.microsoft.useragentSdk.crypto.keys.PrivateKey
import com.microsoft.useragentSdk.crypto.keys.PublicKey
import com.microsoft.useragentSdk.crypto.models.webCryptoApi.JsonWebKey

class EllipticCurvePrivateKey (key: JsonWebKey): PrivateKey(key) {
    var crv = key.crv
    var x = key.x
    var y = key.y
    override var kty = KeyType.EllipticCurve
    override var alg: String? = if (key.alg != null) key.alg!! else "ES256K"
    var d = key.d

    override fun getPublicKey(): PublicKey {
        return EllipticCurvePublicKey(
            JsonWebKey(
                kty = this.kty.value,
                alg = this.alg,
                crv = this.crv,
                x = this.x,
                y = this.y
            )
        )
    }
}