package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.util.Base64Url

abstract class Key(
        val kid: String,
        val kty: KeyType,
        val use: KeyUse,
        val key_ops: List<KeyUsage>,
        val alg: String
) {
    /**
     * Gets the minimum JWK with parameters in alphabetical order as specified by JWK Thumbprint
     * @see https://tools.ietf.org/html/rfc7638
     */
    abstract fun minimumAlphabeticJwk(): String

    /**
     * Obtains the thumbprint for the jwk parameter
     * @param jwk JSON object representation of a JWK
     * @see https://tools.ietf.org/html/rfc7638
     */
    fun getThumbprint(crypto: CryptoOperations, sha: Algorithm = Sha.SHA512.algorithm): String {
        // construct a JSON object with only required fields
        val json = this.minimumAlphabeticJwk()
        val jsonUtf8 = json.toByteArray()
        val digest = crypto.subtleCryptoFactory.getMessageDigest(sha.name, SubtleCryptoScope.PUBLIC)
        val hash = digest.digest(sha, jsonUtf8)
        // undocumented, but assumed base64url of hash is returned
        return Base64Url.encode(hash)
    }
}