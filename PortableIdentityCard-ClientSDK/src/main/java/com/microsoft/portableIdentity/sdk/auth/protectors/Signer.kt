package com.microsoft.portableIdentity.sdk.auth.protectors

import com.microsoft.portableIdentity.sdk.DidSdkConfig
import com.microsoft.portableIdentity.sdk.auth.models.ResponseContent
import com.microsoft.portableIdentity.sdk.crypto.protocols.jose.jws.JwsToken
import com.microsoft.portableIdentity.sdk.utilities.BaseLogger
import com.microsoft.portableIdentity.sdk.utilities.Serializer
import java.security.KeyStore

/**
 * Protector Class that can protect responseContent by signing.
 *
 * @param keyReference to sign token with.
 */
class Signer(private val keyReference: String): IProtector {

    override fun protect(responseContent: ResponseContent) : JwsToken {
        val content = responseContent.stringify()
        return sign(content)
    }

    /**
     * Sign content with keyReference.
     *
     * @param content payload string to wrap in JWS.
     * @param headers optional headers to add to token.
     *
     * @return JwsToken
     */
    fun sign(content: String, headers: Map<String, String> = emptyMap()) : JwsToken {
        val token = JwsToken(content, logger = BaseLogger)
        token.sign(keyReference, DidSdkConfig.didManager.getCryptoOperation(), headers)
        return token
    }
}