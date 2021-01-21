package com.microsoft.did.sdk.crypto

import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.util.log.SdkLog

class CryptoOps {
    fun sign(payload: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null): ByteArray {
        SdkLog.d("Signing with $signingKeyReference")
        val key = keyStore.getKey(signingKeyReference)



//        val alg = algorithm ?: privateKey.alg
        val subtle = subtleCryptoFactory.getMessageSigner(key.alg.name, SubtleCryptoScope.PRIVATE)
        return subtle.sign(alg, key, payload)
    }
}