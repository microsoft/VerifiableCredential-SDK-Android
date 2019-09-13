package com.microsoft.did.sdk.crypto.protocols.jose.jws

/**
 * Class for containing JWS token operations.
 * @class
 */
class JwsToken {

    /**
     * Serialize a JWS token object from token.
     */
    fun serialize () {}

    /**
     * Deserialize a JWS token object
     */
    fun deserialize() {}

    /**
     * Signs contents using the given private key in JWK format
     * @param payload to sign
     * @param signingKeyReference reference to signing key
     */
    fun sign(payload: String, signingKeyReference: String) {}

}