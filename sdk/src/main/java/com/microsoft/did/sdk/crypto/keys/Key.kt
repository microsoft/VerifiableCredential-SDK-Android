package com.microsoft.did.sdk.crypto.keys

import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.models.KeyUse
import com.microsoft.did.sdk.crypto.models.Sha
import com.microsoft.did.sdk.crypto.models.webCryptoApi.KeyUsage
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.util.Base64Url

sealed class Key(
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

sealed class PublicKey(
        kid: String,
        kty: KeyType,
        use: KeyUse,
        key_ops: List<KeyUsage> = emptyList(),
        alg: String
) : Key(kid, kty, use, key_ops, alg) {

}

sealed class PrivateKey(
        kid: String,
        kty: KeyType,
        use: KeyUse,
        key_ops: List<KeyUsage> = emptyList(),
        alg: String
) : Key(kid, kty, use, key_ops, alg) {

    /**
     * Gets the corresponding public key
     * @returns The corresponding {@link PublicKey}
     */
    abstract fun getPublicKey(): PublicKey

    override fun minimumAlphabeticJwk(): String {
        return this.getPublicKey().minimumAlphabeticJwk()
    }
}

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