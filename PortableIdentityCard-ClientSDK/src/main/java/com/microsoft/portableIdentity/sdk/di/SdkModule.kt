// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.di

import android.content.Context
import androidx.room.Room
import com.microsoft.portableIdentity.sdk.crypto.CryptoOperations
import com.microsoft.portableIdentity.sdk.crypto.keyStore.AndroidKeyStore
import com.microsoft.portableIdentity.sdk.crypto.keyStore.KeyStore
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.SubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.models.webCryptoApi.W3cCryptoApiConstants
import com.microsoft.portableIdentity.sdk.crypto.plugins.AndroidSubtle
import com.microsoft.portableIdentity.sdk.crypto.plugins.EllipticCurveSubtleCrypto
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoMapItem
import com.microsoft.portableIdentity.sdk.crypto.plugins.SubtleCryptoScope
import com.microsoft.portableIdentity.sdk.registrars.IRegistrar
import com.microsoft.portableIdentity.sdk.registrars.SidetreeRegistrar
import com.microsoft.portableIdentity.sdk.repository.SdkDatabase
import com.microsoft.portableIdentity.sdk.resolvers.HttpResolver
import com.microsoft.portableIdentity.sdk.resolvers.IResolver
import com.microsoft.portableIdentity.sdk.utilities.Logger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class SdkModule {

    @Provides
    @Singleton
    fun defaultCryptoOperations(
        subtleCrypto: SubtleCrypto,
        ecSubtle: EllipticCurveSubtleCrypto,
        keyStore: KeyStore,
        logger: Logger
    ): CryptoOperations {
        val defaultCryptoOperations = CryptoOperations(subtleCrypto, keyStore, logger)
        defaultCryptoOperations.subtleCryptoFactory.addMessageSigner(
            name = W3cCryptoApiConstants.EcDsa.value,
            subtleCrypto = SubtleCryptoMapItem(ecSubtle, SubtleCryptoScope.All)
        )
        return defaultCryptoOperations
    }

    @Provides
    @Singleton
    fun defaultResolver(resolver: HttpResolver): IResolver {
        return resolver
    }

    @Provides
    @Singleton
    fun defaultRegistrar(registrar: SidetreeRegistrar): IRegistrar {
        return registrar
    }

    @Provides
    @Singleton
    fun defaultSubtleCrypto(subtle: AndroidSubtle): SubtleCrypto {
        return subtle
    }

    @Provides
    @Singleton
    fun defaultKeyStore(keyStore: AndroidKeyStore): KeyStore {
        return keyStore
    }

    @Provides
    @Singleton
    fun sdkDatabase(context: Context): SdkDatabase {
        return Room.databaseBuilder(context, SdkDatabase::class.java, "PortableIdentity-db")
            .fallbackToDestructiveMigration() // TODO: we don't want this here as soon as we go into production
            .build()
    }
}