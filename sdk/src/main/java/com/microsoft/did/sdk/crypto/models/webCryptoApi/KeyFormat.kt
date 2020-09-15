package com.microsoft.did.sdk.crypto.models.webCryptoApi

/**
 * Specifies a serialization format for a key.
 */
enum class KeyFormat(var value: String) {
    /** An unformatted sequence of bytes. Intended for secret keys. */
    Raw("raw"),

    /** The DER encoding of the PrivateKeyInfo structure from RFC 5208. */
    Pkcs8("pkcs8"),

    /** The DER encoding of the SubjectPublicKeyInfo structure from RFC 5280. */
    Spki("spki"),

    /** The key is a JsonWebKey dictionary encoded as a JavaScript object */
    Jwk("jwk")
}