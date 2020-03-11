package com.microsoft.portableIdentity.sdk.crypto.keys

import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.portableIdentity.sdk.utilities.ILogger

abstract class PrivateKey(key: JsonWebKey, logger: ILogger): PublicKey(key, logger = logger) {
    /**
     * Default Sign Algorithm for JWK 'alg' field
     */
    override var alg: String? = if (key.alg != null) key.alg else "none"

    /**
     * Gets the corresponding public key
     * @returns The corresponding {@link PublicKey}
     */
    abstract fun getPublicKey (): PublicKey

    override fun minimumAlphabeticJwk(): String {
        return this.getPublicKey().minimumAlphabeticJwk()
    }
}