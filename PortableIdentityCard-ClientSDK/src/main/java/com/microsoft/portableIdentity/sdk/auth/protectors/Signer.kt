package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger

/**
 * Class that can protect some content by signing.
 */
object Signer {

    /**
     * Sign content with keyReference.
     *
     * @param payload string to wrap in JWS.
     * @param keyReference key reference for key to be used to sign payload.
     * @param additionalHeaders optional headers to add to token.
     *
     * @return JwsToken
     */
    fun sign(payload: String, keyReference: String = "defaultSigningKey", additionalHeaders: Map<String, String> = emptyMap()) : JwsToken {
        val token = JwsToken(payload, logger = BaseLogger)
        token.sign(keyReference, DidSdkConfig.identityManager.cryptoOperations, additionalHeaders)
        return token
    }
}