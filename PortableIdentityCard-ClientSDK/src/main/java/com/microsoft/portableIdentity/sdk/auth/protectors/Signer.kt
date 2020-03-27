package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import javax.inject.Inject

/**
 * Class that can protect some content by signing.
 */
class Signer @Inject constructor(
    private val cryptoOperations: CryptoOperations
) {

    /**
     * Sign content with keyReference.
     *
     * @param payload string to wrap in JWS.
     * @param keyReference key reference for key to be used to sign payload.
     * @param additionalHeaders optional headers to add to token.
     *
     * @return JwsToken
     */
    fun sign(payload: String, keyReference: String = "defaultSigningKey", additionalHeaders: Map<String, String> = emptyMap()): JwsToken {
        val token = JwsToken(payload)
        token.sign(keyReference, cryptoOperations, additionalHeaders)
        return token
    }
}