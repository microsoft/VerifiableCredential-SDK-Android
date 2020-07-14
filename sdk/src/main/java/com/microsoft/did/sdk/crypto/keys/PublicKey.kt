package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.toKeyUse
import com.microsoft.did.sdk.crypto.models.webCryptoApi.JsonWebKey
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.models.webCryptoApi.toKeyUsage
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.util.Base64Url
import com.microsoft.did.sdk.util.stringToByteArray

/**
 * Represents a Public Key in JWK format.
 * @class
 * @abstract
 */
abstract class PublicKey(val key: JsonWebKey) : IKeyStoreItem {
    /**
     * Key type
     */
    open var kty: KeyType = toKeyType(key.kty)

    /**
     * Key ID
     */
    override var kid: String = key.kid ?: ""

    /**
     * Intended use
     */
    open var use: KeyUse? = key.use?.let { toKeyUse(it) }

    /**
     * Valid key operations (key_ops)
     */
    open var key_ops: List<KeyUsage>? = key.key_ops?.map { toKeyUsage(it) }

    /**
     * Algorithm intended for use with this key
     */
    open var alg: String? = key.alg

    /**
     * Obtains the thumbprint for the jwk parameter
     * @param jwk JSON object representation of a JWK
     * @see https://tools.ietf.org/html/rfc7638
     */
    fun getThumbprint(crypto: CryptoOperations, sha: Algorithm = Sha.SHA512.algorithm): String {
        // construct a JSON object with only required fields
        val json = this.minimumAlphabeticJwk()
        val jsonUtf8 = stringToByteArray(json)
        val digest = crypto.subtleCryptoFactory.getMessageDigest(sha.name, SubtleCryptoScope.PUBLIC)
        val hash = digest.digest(sha, jsonUtf8)
        // undocumented, but assumed base64url of hash is returned
        return Base64Url.encode(hash)
    }

    /**
     * Gets the minimum JWK with parameters in alphabetical order as specified by JWK Thumbprint
     * @see https://tools.ietf.org/html/rfc7638
     */
    abstract fun minimumAlphabeticJwk(): String

    abstract fun toJWK(): JsonWebKey
}