package com.microsoft.did.sdk.crypto.keys

import android.util.Base64
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import java.security.KeyStore

class AndroidSecretKey private constructor (jwk: JsonWebKey): SecretKey(jwk) {
    constructor (alias: String, entry: KeyStore.SecretKeyEntry): this(JsonWebKey(
            kty = KeyType.Octets.value,
            kid = alias,
            k = Base64.encodeToString(entry.secretKey.encoded, Base64.URL_SAFE)
        ))
}
