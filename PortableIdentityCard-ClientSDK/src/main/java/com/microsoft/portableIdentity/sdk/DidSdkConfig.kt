//// Copyright (c) Microsoft Corporation. All rights reserved
//
package com.microsoft.portableIdentity.sdk
//
//import android.content.Context
//import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
//import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
//import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
//import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
//import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
//import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
//import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
//import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
//import com.microsoft.portableIdentity.sdk.registrars.SidetreeRegistrar
//import com.microsoft.portableIdentity.sdk.repository.InMemoryStore
//import com.microsoft.portableIdentity.sdk.repository.Repository
//import com.microsoft.portableIdentity.sdk.resolvers.HttpResolver
//import com.microsoft.portableIdentity.sdk.resolvers.IResolver
//import com.microsoft.portableIdentity.sdk.utilities.ConsoleLogger
//import com.microsoft.portableIdentity.sdk.utilities.ILogger
//
//class DidSdkConfig(
//    context: Context,
//    internal val logger: ILogger,
//    internal val signatureKeyReference: String,
//    internal val encryptionKeyReference: String,
//    registrationUrl: String,
//    resolverUrl: String
//) {
//
//    /**
//     * This is a helper for static access to the SDK. This should not be needed for projects with proper dependency
//     * injection as this obfuscates dependencies and harms testability
//     */
//    companion object {
//        @JvmStatic
//        lateinit var identityManager: IdentityManager
//            private set
//
//        @JvmStatic
//        lateinit var cardManager: CardManager
//            private set
//
//        @JvmStatic
//        @JvmOverloads
//        fun init(
//            context: Context,
//            logger: ILogger = ConsoleLogger(),
//            registrationUrl: String = "https://beta.ion.microsoft.com/api/1.0/register",
//            resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers",
//            signatureKeyReference: String = "signature",
//            encryptionKeyReference: String = "encryption"
//        ) {
//            val config = DidSdkConfig(context, logger, signatureKeyReference, encryptionKeyReference, registrationUrl, resolverUrl)
//            identityManager = IdentityManager(config)
//            cardManager = CardManager(config)
//        }
//    }
//
//    internal var registrar: IRegistrar
//
//    internal var resolver: IResolver
//
//    internal var cryptoOperations: CryptoOperations
//
//    internal var repository: Repository
//
//    init {
//        val keyStore = AndroidKeyStore(context, logger)
//        val subtleCrypto = AndroidSubtle(keyStore, logger)
//        val ecSubtle = EllipticCurveSubtleCrypto(subtleCrypto, logger)
//        registrar = SidetreeRegistrar(registrationUrl, logger)
//        resolver = HttpResolver(resolverUrl, logger)
//        cryptoOperations = CryptoOperations(subtleCrypto, keyStore, logger)
//        cryptoOperations.subtleCryptoFactory.addMessageSigner(
//            name = W3cCryptoApiConstants.EcDsa.value,
//            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
//        )
//        repository = Repository(InMemoryStore())
//    }
//}