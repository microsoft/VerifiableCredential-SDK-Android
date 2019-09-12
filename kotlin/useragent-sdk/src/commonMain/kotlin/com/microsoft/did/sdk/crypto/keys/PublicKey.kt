package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.KeyUse
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.Transient

/**
 * Represents a Public Key in JWK format.
 * @class
 * @abstract
 */
//@Serializable
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
     * @see https://tools.ietf.org/html/rfc7638
     */
    abstract fun getThumbprint (): String;
}