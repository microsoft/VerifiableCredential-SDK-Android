package com.microsoft.did.sdk.crypto.keys.ellipticCurve

import com.microsoft.did.sdk.crypto.keys.KeyType
import com.microsoft.did.sdk.crypto.keys.PublicKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.utilities.Base64Url

class EllipticCurvePublicKey(key: JsonWebKey): PublicKey(key) {
     override fun getThumbprint(): String {
         // construct a JSON object with only required fields
         val json = "{\"crv\":\"$crv\",\"kty\":\"${kty.value}\",\"x\":\"$x\",\"y\":\"$y\"}"
         val jsonUtf8 = ByteArray(json.length)
         json.forEachIndexed() { index, character ->
             jsonUtf8[index] = character.toByte()
         }
         TODO("Get a SubtleCrypto or some way to hash, somehow.")
         val subtle: SubtleCrypto
         val hash = subtle.digest(Algorithm("SHA-512"), jsonUtf8)
         // undocumented, but assumed base64url of hash is returned
         return Base64Url.encode(hash)
     }

     var crv = key.crv
     var x = key.x
     var y = key.y
     override var kty = KeyType.EllipticCurve
 }