// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.did.sdk

import android.content.Context
import androidx.room.Room
import com.microsoft.did.sdk.crypto.CryptoOperations
import com.microsoft.did.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.did.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.did.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.did.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.did.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.did.sdk.registrars.IRegistrar
import com.microsoft.did.sdk.registrars.SidetreeRegistrar
import com.microsoft.did.sdk.persistance.SdkDatabase
import com.microsoft.did.sdk.persistance.repository.VerifiableCredentialRepository
import com.microsoft.did.sdk.resolvers.HttpResolver
import com.microsoft.did.sdk.resolvers.IResolver
import com.microsoft.did.sdk.utilities.ConsoleLogger
import com.microsoft.did.sdk.utilities.ILogger

class DidSdkConfig(
    context: Context,
    internal val logger: ILogger,
    internal val signatureKeyReference: String,
    internal val encryptionKeyReference: String,
    registrationUrl: String,
    resolverUrl: String
) {

    /**
     * This is a helper for static access to the SDK. This should not be needed for projects with proper dependency
     * injection as this obfuscates dependencies and harms testability
     */
    companion object {
        @JvmStatic
        lateinit var didManager: DidManager
            private set

        @JvmStatic
        lateinit var picManager: PicManager
            private set

        @JvmStatic
        @JvmOverloads
        fun init(
            context: Context,
            logger: ILogger = ConsoleLogger(),
            registrationUrl: String = "https://beta.ion.microsoft.com/api/1.0/register",
            resolverUrl: String = "https://beta.discover.did.microsoft.com/1.0/identifiers",
            signatureKeyReference: String = "signature",
            encryptionKeyReference: String = "encryption"
        ) {
            val config = DidSdkConfig(context, logger, signatureKeyReference, encryptionKeyReference, registrationUrl, resolverUrl)
            didManager = DidManager(config)
            picManager = PicManager(config)
        }
    }

    internal var registrar: IRegistrar

    internal var resolver: IResolver

    internal var cryptoOperations: CryptoOperations

    internal var vcRepository: VerifiableCredentialRepository

    init {
        val keyStore = AndroidKeyStore(context, logger)
        val subtleCrypto = AndroidSubtle(keyStore, logger)
        val ecSubtle = EllipticCurveSubtleCrypto(subtleCrypto, logger)
        registrar = SidetreeRegistrar(registrationUrl, logger)
        resolver = HttpResolver(resolverUrl, logger)
        cryptoOperations = CryptoOperations(subtleCrypto, keyStore, logger)
        cryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
        val db = Room.databaseBuilder(context, SdkDatabase::class.java, "PortableIdentity-db").build()
        vcRepository = VerifiableCredentialRepository(db)

    }
}