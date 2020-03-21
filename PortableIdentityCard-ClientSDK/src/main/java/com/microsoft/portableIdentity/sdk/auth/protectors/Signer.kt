package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger

/**
 * Protector Class that can protect responseContent by signing.
 * TODO(make default signing key)
 * @param keyReference to sign token with.
 */
class Signer(private val keyReference: String = "sigKey",
             private val cryptoOperations: CryptoOperations = DidSdkConfig.didManager.cryptoOperations,
             private val additionalHeaders: Map<String, String> = emptyMap()): Protector {

    override fun protect(contents: String) : JwsToken {
        return sign(contents)
    }

    /**
     * Sign content with keyReference.
     *
     * @param content payload string to wrap in JWS.
     * @param headers optional headers to add to token.
     *
     * @return JwsToken
     */
    fun sign(content: String) : JwsToken {
        val token = JwsToken(content, logger = BaseLogger)
        token.sign(keyReference, cryptoOperations, additionalHeaders)
        return token
    }
}