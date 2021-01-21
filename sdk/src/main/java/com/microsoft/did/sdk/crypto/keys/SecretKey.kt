package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage

class SecretKey(
        kid: String,
        kty: KeyType = KeyType.Octets,
        use: KeyUse,
        key_ops: List<KeyUsage> = emptyList(),
        alg: String,
        val secret: String
) : Key(kid, kty, use, key_ops, alg) {
    override fun minimumAlphabeticJwk(): String {
        TODO("Not yet implemented")
    }
}