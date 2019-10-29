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
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.ILogger

class Agent private constructor (
            registrationUrl: String = defaultRegistrationUrl,
            resolverUrl: String = defaultResolverUrl,
            cryptoOperations: CryptoOperations,
            logger: ILogger): AbstractAgent(
    registrationUrl = registrationUrl,
    resolverUrl = resolverUrl,
    signatureKeyReference = defaultSignatureKeyReference,
    encryptionKeyReference = defaultEncryptionKeyReference,
    cryptoOperations = cryptoOperations,
    logger = logger
    ) {

    companion object {
        fun getInstance(context: Context, logger: ILogger = ConsoleLogger()): Agent {
            val keyStore = AndroidKeyStore(context, logger)
            val subtleCrypto = AndroidSubtle(keyStore, logger)
            val crypto = CryptoOperations(subtleCrypto, keyStore, logger)
            val ecSubtle = EllipticCurveSubtleCrypto(subtleCrypto, logger)
            crypto.subtleCryptoFactory.addMessageSigner(W3cCryptoApiConstants.EcDsa.value,
                SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
            );
            return Agent(
                registrationUrl = "https://beta.core.ion.msidentity.com/",
                cryptoOperations = crypto,
                logger = logger
            )
        }
    }
}
