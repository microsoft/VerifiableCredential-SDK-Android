// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.di

import android.content.Context
import com.microsoft.portableIdentity.sdk.CardManager
import com.microsoft.portableIdentity.sdk.IdentityManager
import dagger.BindsInstance
import dagger.Component
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

/**
 * This interface is used by Dagger to generate the code in `DaggerSdkComponent`. It exposes the dependency graph to
 * the outside. Dagger will expose the type inferred by the return type of the interface function.
 *
 * More information:
 * https://dagger.dev/users-guide
 * https://developer.android.com/training/dependency-injection
 */
@Singleton
@Component(modules = [SdkModule::class])
internal interface SdkComponent {

    fun identityManager(): IdentityManager

    fun cardManager(): CardManager

    @Component.Builder
    interface Builder {
        fun build(): SdkComponent

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun signatureKeyReference(@Named("signatureKeyReference") signatureKeyReference: String): Builder

        @BindsInstance
        fun encryptionKeyReference(@Named("encryptionKeyReference") encryptionKeyReference: String): Builder

        @BindsInstance
        fun resolverUrl(@Named("resolverUrl") resolverUrl: String): Builder

        @BindsInstance
        fun registrationUrl(@Named("registrationUrl") registrationUrl: String): Builder

        @BindsInstance
        fun retrofit(): Builder
    }
}