// Copyright (c) Microsoft Corporation. All rights reserved

package com.microsoft.portableIdentity.sdk.di

import android.content.Context
import com.microsoft.portableIdentity.sdk.CardManager
import com.microsoft.portableIdentity.sdk.IdentityManager
import com.microsoft.portableIdentity.sdk.utilities.Logger
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named
import javax.inject.Singleton

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
        fun logger(logger: Logger): Builder

        @BindsInstance
        fun signatureKeyReference(@Named("signatureKeyReference") signatureKeyReference: String): Builder

        @BindsInstance
        fun encryptionKeyReference(@Named("encryptionKeyReference") encryptionKeyReference: String): Builder

        @BindsInstance
        fun resolverUrl(@Named("resolverUrl") resolverUrl: String): Builder

        @BindsInstance
        fun registrationUrl(@Named("registrationUrl") registrationUrl: String): Builder
    }
}