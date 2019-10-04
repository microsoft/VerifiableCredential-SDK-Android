package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import android.content.Context
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto

class Agent private constructor (
            registrationUrl: String = defaultRegistrationUrl,
            resolverUrl: String = defaultResolverUrl,
            subtleCrypto: SubtleCrypto,
            keyStore: IKeyStore): AbstractAgent(registrationUrl, resolverUrl, keyStore, subtleCrypto) {

    companion object {
        fun getInstance(context: Context): Agent {
            val keyStore = AndroidKeyStore(context)
            val subtleCrypto = AndroidSubtle(keyStore)
            return Agent(
                subtleCrypto = subtleCrypto,
                keyStore = keyStore
            )
        }
    }
}
