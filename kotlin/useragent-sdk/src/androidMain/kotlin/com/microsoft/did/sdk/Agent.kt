package com.microsoft.did.sdk

import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import android.content.Context
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.keyStore.IKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope

class Agent private constructor (
            registrationUrl: String = defaultRegistrationUrl,
            resolverUrl: String = defaultResolverUrl,
            cryptoOperations: CryptoOperations): AbstractAgent(
    registrationUrl = registrationUrl,
    resolverUrl = resolverUrl,
    signatureKeyReference = defaultSignatureKeyReference,
    encryptionKeyReference = defaultEncryptionKeyReference,
    cryptoOperations = cryptoOperations
    ) {

    companion object {
        fun getInstance(context: Context): Agent {
            val keyStore = AndroidKeyStore(context)
            val subtleCrypto = AndroidSubtle(keyStore)
            val crypto = CryptoOperations(subtleCrypto, keyStore)
            val ecSubtle = EllipticCurveSubtleCrypto(subtleCrypto)
            crypto.subtleCryptoFactory.addMessageSigner(W3cCryptoApiConstants.EcDsa.value,
                SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
            );
            return Agent(
                registrationUrl = "https://beta.core.ion.msidentity.com/",
                cryptoOperations = crypto
            )
        }
    }
}
