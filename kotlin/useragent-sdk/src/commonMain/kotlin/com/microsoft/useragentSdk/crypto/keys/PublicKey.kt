package com.microsoft.useragentSdk.crypto.keys

import com.microsoft.useragentSdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.useragentSdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.useragentSdk.crypto.models.KeyUse

/**
 * Represents a Public Key in JWK format.
 * @class
 * @abstract
 * @hideconstructor
 */
abstract class PublicKey (key: JsonWebKey) {
    /**
     * Key type
     */
    open var kty: KeyType = KeyType.valueOf(key.kty)

    /**
     * Key ID
     */
    open var kid: String? = key.kid

    /**
     * Intended use
     */
    open var use: KeyUse? = key.use?.let { KeyUse.valueOf(it) }

    /**
     * Valid key operations (key_ops)
     */
    open var key_ops: List<KeyUsage>? = key.key_ops?.map { KeyUsage.valueOf(it) }

    /**
     * Algorithm intended for use with this key
     */
    open var alg: String? = key.alg

    /**
     * Obtains the thumbprint for the jwk parameter
     * @param jwk JSON object representation of a JWK
     */
    abstract fun getThumbprint (): String;
}