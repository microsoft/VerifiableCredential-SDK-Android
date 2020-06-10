package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey

abstract class PrivateKey(key: JsonWebKey) : PublicKey(key) {
    /**
     * Default Sign Algorithm for JWK 'alg' field
     */
    override var alg: String? = if (key.alg != null) key.alg else "none"

    /**
     * Gets the corresponding public key
     * @returns The corresponding {@link PublicKey}
     */
    abstract fun getPublicKey(): PublicKey

    override fun minimumAlphabeticJwk(): String {
        return this.getPublicKey().minimumAlphabeticJwk()
    }
}