package com.microsoft.did.sdk.crypto

import android.content.Context
import com.microsoft.did.sdk.crypto.keyStore.EncryptedKeyStore
import com.microsoft.did.sdk.crypto.keyStore.KeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.algorithms.Algorithm
import com.microsoft.did.sdk.crypto.plugins.Secp256k1Provider
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.crypto.plugins.subtleCrypto.Provider
import com.microsoft.did.sdk.util.log.SdkLog
import kotlinx.serialization.json.Json

class CryptoOps(context: Context, serializer: Json) {

    private val providers = mapOf<String, Provider>(
            "secp256k1" to Secp256k1Provider()
    )

    private val encryptedKeyStore: KeyStore = EncryptedKeyStore(context, serializer)

    fun sign(payload: ByteArray, signingKeyReference: String, algorithm: Algorithm? = null): ByteArray {
        SdkLog.d("Signing with $signingKeyReference")
        val key = keyStore.getKey(signingKeyReference)

        val subtle = subtleCryptoFactory.getMessageSigner(key.alg.name, SubtleCryptoScope.PRIVATE)
        return subtle.sign(alg, key, payload)
    }


}