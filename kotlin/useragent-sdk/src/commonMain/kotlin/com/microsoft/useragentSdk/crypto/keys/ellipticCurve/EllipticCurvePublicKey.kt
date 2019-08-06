package com.microsoft.useragentSdk.crypto.keys.ellipticCurve

import com.microsoft.useragentSdk.crypto.keys.KeyType
import com.microsoft.useragentSdk.crypto.keys.PublicKey
import com.microsoft.useragentSdk.crypto.models.JsonWebKey

 class EllipticCurvePublicKey(key: JsonWebKey): PublicKey(key) {
     override fun getThumbprint(): String {
         return ellipticCurvePublicKeyThumbprint(this)
     }

     var crv = key.crv
     var x = key.x
     var y = key.y
     override var kty = KeyType.EllipticCurve
 }

expect fun ellipticCurvePublicKeyThumbprint(key: EllipticCurvePublicKey): String;